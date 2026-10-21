package com.kilo.kilo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la base de datos.
 *  - Lecturas reactivas -> Flow<T>: la UI se actualiza sola cuando cambia la tabla.
 *  - Escrituras -> suspend fun: Room las ejecuta fuera del hilo principal.
 */
@Dao
abstract class KiloDao {

    // ------------------------------------------------------------ lecturas
    @Transaction
    @Query("SELECT * FROM vehiculos ORDER BY id")
    abstract fun observarVehiculos(): Flow<List<VehiculoConDetalle>>

    @Transaction
    @Query("SELECT * FROM vehiculos ORDER BY id")
    abstract suspend fun obtenerVehiculos(): List<VehiculoConDetalle>

    @Query("SELECT COUNT(*) FROM vehiculos")
    abstract suspend fun contarVehiculos(): Int

    @Query("SELECT COUNT(*) FROM vehiculos WHERE activo = 1")
    abstract suspend fun contarActivos(): Int

    @Query("SELECT id FROM vehiculos ORDER BY id LIMIT 1")
    abstract suspend fun primerVehiculoId(): Long?

    // ------------------------------------------------------------ escrituras simples
    @Insert
    abstract suspend fun insertarVehiculo(vehiculo: VehiculoEntity)

    @Insert
    abstract suspend fun insertarPiezas(piezas: List<PiezaEntity>)

    @Insert
    abstract suspend fun insertarRegistros(registros: List<RegistroEntity>)

    @Insert
    abstract suspend fun insertarRegistro(registro: RegistroEntity)

    @Query("UPDATE vehiculos SET km = :km WHERE id = :id")
    abstract suspend fun actualizarKm(id: Long, km: Int)

    /** Deja activo solo al vehículo [id] y desactiva a los demás, en una sola sentencia. */
    @Query("UPDATE vehiculos SET activo = CASE WHEN id = :id THEN 1 ELSE 0 END")
    abstract suspend fun marcarActivo(id: Long)

    @Query("DELETE FROM vehiculos WHERE id = :id")
    abstract suspend fun borrarVehiculo(id: Long)

    @Query("DELETE FROM registros WHERE id = :id")
    abstract suspend fun eliminarRegistro(id: Long)

    // ------------------------------------------------------------ operaciones atómicas
    /** Inserta vehículo + piezas + registros todo junto (o nada) y lo deja activo. */
    @Transaction
    open suspend fun agregarVehiculoActivo(
        vehiculo: VehiculoEntity,
        piezas: List<PiezaEntity>,
        registros: List<RegistroEntity>
    ) {
        insertarVehiculo(vehiculo.copy(activo = false))
        insertarPiezas(piezas)
        insertarRegistros(registros)
        marcarActivo(vehiculo.id)
    }

    /** Datos de ejemplo la primera vez que se abre la app (solo si la tabla está vacía). */
    @Transaction
    open suspend fun sembrarSiVacio(
        vehiculo: VehiculoEntity,
        piezas: List<PiezaEntity>,
        registros: List<RegistroEntity>
    ) {
        if (contarVehiculos() == 0) agregarVehiculoActivo(vehiculo, piezas, registros)
    }

    /** Borra un vehículo; si era el activo, el primero que quede pasa a ser el activo. */
    @Transaction
    open suspend fun eliminarVehiculoYReasignarActivo(id: Long) {
        borrarVehiculo(id)
        if (contarActivos() == 0) {
            primerVehiculoId()?.let { marcarActivo(it) }
        }
    }
}
