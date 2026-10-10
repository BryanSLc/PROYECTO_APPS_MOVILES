package com.kilo.kilo.ui.agregar_vehiculo

data class MantenimientoInicial(
    val nombre: String,
    val km: String = ""
)

data class AgregarVehiculoState(
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val placa: String = "",
    val kilometraje: String = "",
    val tipos: List<String> = listOf("Sedán", "SUV", "Pickup", "Moto"),
    val tipoSeleccionado: String = "Sedán",
    val mantenimientos: List<MantenimientoInicial> = listOf(
        MantenimientoInicial("Aceite del motor"),
        MantenimientoInicial("Filtro de aceite"),
        MantenimientoInicial("Llantas"),
        MantenimientoInicial("Frenos"),
        MantenimientoInicial("Batería")
    ),
    val error: String? = null
) {
    val puedeGuardar: Boolean
        get() = marca.isNotBlank() &&
                modelo.isNotBlank() &&
                placa.isNotBlank() &&
                kilometraje.isNotBlank()
}