package com.kilo.kilo.data

import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato del repositorio de Kilo (equivale a ItemsRepository del ejemplo).
 *
 * Los ViewModels SOLO conocen esta interfaz. Quién la implementa lo decide Hilt
 * en di/DataModule.kt; nadie crea el repositorio a mano.
 *
 * Las funciones que devuelven String? responden con un mensaje de error
 * o con null cuando todo salió bien.
 */
interface KiloRepository {

    /** Todos los vehículos y cuál es el activo. Se vuelve a emitir cada vez que algo cambia. */
    val datos: StateFlow<DatosKilo>

    // ------------------------------------------------------------ vehículos
    fun actualizarKm(nuevoKm: Int): String?

    fun agregarVehiculo(
        nombre: String,
        placa: String,
        carroceria: String,
        km: Int,
        piezas: List<NuevaPieza>
    ): String?

    fun seleccionarVehiculo(id: Long)

    fun eliminarVehiculo(id: Long): String?

    // ------------------------------------------------------------ mantenimientos
    fun registrarMantenimiento(piezaId: Long, fecha: LocalDate, km: Int, nota: String): String?

    /** NUEVO: borra un registro del historial del vehículo activo. */
    fun eliminarRegistro(registroId: Long): String?
}