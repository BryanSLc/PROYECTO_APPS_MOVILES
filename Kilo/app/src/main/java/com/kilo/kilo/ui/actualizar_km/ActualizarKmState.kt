package com.kilo.kilo.ui.actualizar_km

data class ActualizarKmState(
    val vehiculo: String = "",
    val kmActual: Int = 0,
    val kmNuevo: String = ""
) {
    val puedeConfirmar: Boolean
        get() = kmNuevo.isNotBlank()
}