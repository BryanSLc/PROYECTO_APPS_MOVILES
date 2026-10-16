package com.kilo.kilo.ui.mantenimiento

import com.kilo.kilo.ui.piezasporcategorias.EstadoPieza

data class RegistroDetalle(val km: String, val detalle: String)

data class DetalleMantenimientoState(
    // Los valores por defecto solo sirven para el @Preview; en la app salen del cálculo
    val titulo: String = "Aceite del motor",
    val intervalo: String = "Cada 10,000 km o 6 meses, lo que ocurra primero",
    val estado: EstadoPieza = EstadoPieza.PROXIMOS,
    val etiquetaEstado: String = "Próximo",
    val progreso: Float = 0.85f,
    val ultimoKm: String = "60,000 km",
    val metaKm: String = "70,000 km",
    val proximoCambio: String = "70,000 km",
    val fechaLimite: String = "20 nov 2026",
    val historial: List<RegistroDetalle> = listOf(
        RegistroDetalle("60,000 km", "20 may 2026 · Sintético 5W-30"),
        RegistroDetalle("50,100 km", "22 sep 2025 · Taller Central"),
        RegistroDetalle("40,400 km", "14 feb 2025 · Semisintético")
    ),
    val mostrarRegistro: Boolean = false,
    /** id (Long, como texto) de la pieza que se está viendo; se pasa a la hoja de Registrar. */
    val tipoClave: String = "1"
)