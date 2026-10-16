package com.kilo.kilo.ui.registrarmantenimiento

import com.kilo.kilo.data.PIEZAS_BASE

data class RegistrarMantenimientoState(
    // Los valores por defecto solo sirven para el @Preview; en la app salen del vehículo activo
    val tipos: List<String> = PIEZAS_BASE.map { it.nombre },
    val tipoSeleccionado: String = PIEZAS_BASE.first().nombre,
    val fecha: String = "13/09/2026",
    val kmDelCambio: String = "68450",
    val notas: String = "",
    val error: String? = null
)
