package com.kilo.kilo.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de Kilo.
 *
 * Los ViewModels SOLO conocen esta interfaz. Quién la implementa lo decide Hilt
 * en di/DataModule.kt; nadie crea el repositorio a mano. Room queda oculto:
 * aquí solo aparecen modelos de dominio.
 *
 *  - Lectura: un Flow que se vuelve a emitir cada vez que cambia la base de datos.
 *  - Escritura: funciones suspend (no bloquean el hilo principal).
 *
 * Las funciones que devuelven String? responden con un mensaje de error
 * o con null cuando todo salió bien.
 */
interface KiloRepository {

    /** Todos los vehículos y cuál es el activo. Room es la única fuente de verdad. */
    val datos: Flow<DatosKilo>

    // ------------------------------------------------------------ vehículos
    suspend fun actualizarKm(nuevoKm: Int): String?

    suspend fun agregarVehiculo(
        nombre: String,
        placa: String,
        carroceria: String,
        km: Int,
        piezas: List<NuevaPieza>
    ): String?

    suspend fun seleccionarVehiculo(id: Long)

    suspend fun eliminarVehiculo(id: Long): String?

    // ------------------------------------------------------------ mantenimientos
    suspend fun registrarMantenimiento(piezaId: Long, fecha: LocalDate, km: Int, nota: String): String?

    /** Borra un registro del historial del vehículo activo. */
    suspend fun eliminarRegistro(registroId: Long): String?
}
