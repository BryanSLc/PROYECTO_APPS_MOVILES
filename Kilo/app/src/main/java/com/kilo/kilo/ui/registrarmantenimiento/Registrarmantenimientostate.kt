package com.kilo.kilo.ui.registrarmantenimiento

data class RegistrarMantenimientoState(
    val tipos: List<String> = listOf("Aceite del motor", "Filtro de aceite", "Llantas", "Frenos", "Batería"),
    val tipoSeleccionado: String = "Aceite del motor",
    val fecha: String = "13/09/2026",
    val kmDelCambio: String = "68450",
    val notas: String = ""
)