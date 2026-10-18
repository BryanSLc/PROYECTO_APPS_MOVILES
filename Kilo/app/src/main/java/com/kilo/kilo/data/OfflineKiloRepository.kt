package com.kilo.kilo.data

import com.kilo.kilo.data.local.KiloDao
import com.kilo.kilo.data.local.PiezaEntity
import com.kilo.kilo.data.local.RegistroEntity
import com.kilo.kilo.data.local.VehiculoEntity
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

// ===================================================================
//  REPOSITORIO (implementación): Room es la ÚNICA fuente de verdad.
//  Todo cambio se escribe en Room y la UI lo recibe por el Flow del DAO.
// ===================================================================

@Singleton
class OfflineKiloRepository @Inject constructor(
    private val dao: KiloDao
) : KiloRepository {

    private val ultimoId = AtomicLong(0L)

    private fun nuevoId(): Long = ultimoId.updateAndGet { max(System.currentTimeMillis(), it + 1) }

    override val datos: Flow<DatosKilo> = dao.observarVehiculos()
        .onStart { sembrarSiVacio() }
        .filter { it.isNotEmpty() }
        .map { it.aDatosKilo() }

    /** Foto actual de la base de datos (para validar antes de escribir). */
    private suspend fun actual(): DatosKilo {
        sembrarSiVacio()
        return dao.obtenerVehiculos().aDatosKilo()
    }

    // ------------------------------------------------------------ vehículos

    /** Cambia el kilometraje del vehículo activo. Devuelve un mensaje de error o null si salió bien. */
    override suspend fun actualizarKm(nuevoKm: Int): String? {
        val v = actual().vehiculo
        if (nuevoKm < v.km) {
            return "No puede ser menor al actual (${v.km.conComas()} km)"
        }
        dao.actualizarKm(v.id, nuevoKm)
        return null
    }

    /** Agrega un vehículo con sus piezas y lo deja como vehículo activo. */
    override suspend fun agregarVehiculo(
        nombre: String,
        placa: String,
        carroceria: String,
        km: Int,
        piezas: List<NuevaPieza>
    ): String? {
        val actual = actual()
        if (nombre.isBlank()) return "Escribe la marca y el modelo"
        if (placa.isBlank()) return "Escribe la placa"
        if (km < 0) return "El kilometraje no es válido"
        if (actual.vehiculos.any { it.placa.equals(placa.trim(), ignoreCase = true) }) {
            return "Ya existe un vehículo con esa placa"
        }
        if (piezas.isEmpty()) return "Agrega al menos una pieza"

        val nombres = mutableSetOf<String>()
        for (p in piezas) {
            val n = p.nombre.trim()
            if (n.isEmpty()) return "Todas las piezas necesitan un nombre"
            if (!nombres.add(n.lowercase())) return "La pieza \"$n\" está repetida"
            if (p.intervaloKm <= 0) return "\"$n\": el límite de km debe ser mayor a 0"
            if (p.intervaloMeses != null && p.intervaloMeses <= 0) {
                return "\"$n\": los meses deben ser mayores a 0"
            }
            if (p.ultimoKm != null && p.ultimoKm > km) {
                return "\"$n\": el último cambio no puede ser mayor al kilometraje actual"
            }
        }

        val hoy = LocalDate.now()
        val vehiculoId = nuevoId()
        val piezasEntidad = mutableListOf<PiezaEntity>()
        val registrosEntidad = mutableListOf<RegistroEntity>()
        piezas.forEach { p ->
            val piezaId = nuevoId()
            piezasEntidad += PiezaEntity(piezaId, vehiculoId, p.nombre.trim(), p.intervaloKm, p.intervaloMeses)
            if (p.ultimoKm != null) {
                registrosEntidad += RegistroEntity(
                    id = nuevoId(),
                    vehiculoId = vehiculoId,
                    piezaId = piezaId,
                    fecha = hoy,
                    km = p.ultimoKm,
                    nota = "Registro inicial"
                )
            }
        }

        val vehiculo = VehiculoEntity(
            id = vehiculoId,
            nombre = nombre.trim(),
            placa = placa.trim().uppercase(),
            carroceria = carroceria,
            km = km,
            activo = true
        )
        dao.agregarVehiculoActivo(vehiculo, piezasEntidad, registrosEntidad)
        return null
    }

    override suspend fun seleccionarVehiculo(id: Long) {
        val actual = actual()
        if (actual.vehiculos.none { it.id == id } || actual.vehiculoActivoId == id) return
        dao.marcarActivo(id)
    }

    /** No permite borrar el último vehículo. */
    override suspend fun eliminarVehiculo(id: Long): String? {
        val actual = actual()
        if (actual.vehiculos.none { it.id == id }) return null
        if (actual.vehiculos.size <= 1) return "Debe quedar al menos un vehículo"
        dao.eliminarVehiculoYReasignarActivo(id)
        return null
    }

    // ------------------------------------------------------------ mantenimientos

    /**
     * Registra que una pieza del vehículo activo se cambió en [km].
     * NO modifica el kilometraje del vehículo: eso solo se hace con [actualizarKm].
     */
    override suspend fun registrarMantenimiento(
        piezaId: Long,
        fecha: LocalDate,
        km: Int,
        nota: String
    ): String? {
        val v = actual().vehiculo
        if (v.piezas.none { it.id == piezaId }) return "No se encontró la pieza"
        if (fecha.isAfter(LocalDate.now())) return "La fecha no puede ser futura"
        if (km < 0) return "El kilometraje no es válido"
        if (km > v.km) {
            return "No puede ser mayor al kilometraje actual (${v.km.conComas()} km). " +
                    "Primero actualiza el kilometraje."
        }
        dao.insertarRegistro(
            RegistroEntity(
                id = nuevoId(),
                vehiculoId = v.id,
                piezaId = piezaId,
                fecha = fecha,
                km = km,
                nota = nota.trim()
            )
        )
        return null
    }

    /** Borra un registro del historial del vehículo activo. */
    override suspend fun eliminarRegistro(registroId: Long): String? {
        val v = actual().vehiculo
        if (v.registros.none { it.id == registroId }) return "No se encontró el registro"
        dao.eliminarRegistro(registroId)
        return null
    }

    // ------------------------------------------------------------ datos iniciales

    private suspend fun sembrarSiVacio() {
        if (dao.contarVehiculos() > 0) return

        val vehiculoId = 1L
        val piezas = PIEZAS_BASE.mapIndexed { i, p ->
            PiezaEntity((i + 1).toLong(), vehiculoId, p.nombre, p.intervaloKm, p.intervaloMeses)
        }
        // ids de piezas: 1 aceite, 2 filtro, 3 llantas, 4 frenos, 5 batería
        fun registro(id: Long, piezaId: Long, fecha: LocalDate, km: Int, nota: String) =
            RegistroEntity(id, vehiculoId, piezaId, fecha, km, nota)

        val registros = listOf(
            registro(1, 1, LocalDate.of(2026, 5, 20), 60_000, "Sintético 5W-30"),
            registro(2, 1, LocalDate.of(2025, 9, 22), 50_100, "Taller Central"),
            registro(3, 1, LocalDate.of(2025, 2, 14), 40_400, "Semisintético"),
            registro(4, 2, LocalDate.of(2025, 10, 10), 56_000, "Original"),
            registro(5, 4, LocalDate.of(2026, 1, 20), 55_000, "Balatas delanteras"),
            registro(6, 3, LocalDate.of(2025, 6, 15), 48_000, "Juego nuevo")
        )
        val mazda = VehiculoEntity(vehiculoId, "Mazda 3 2019", "PBX-482", "Sedán", 68_450, activo = true)
        dao.sembrarSiVacio(mazda, piezas, registros)
    }
}
