package com.kilo.kilo.ui.mis_vehiculos

enum class EstadoMantenimiento { VENCIDO, PROXIMO, AL_DIA, SIN_DATOS }

data class MantenimientoUi(
    val nombre: String,
    val estado: EstadoMantenimiento
)

data class VehiculoUi(
    val id: Int,
    val nombre: String,
    val placa: String,
    val tipo: String,
    val kilometraje: String,
    val mantenimientos: List<MantenimientoUi> = emptyList()
) {
    val vencidos: Int
        get() = mantenimientos.count { it.estado == EstadoMantenimiento.VENCIDO }
}

data class MisVehiculosState(
    val cargando: Boolean = false,
    val vehiculos: List<VehiculoUi> = emptyList(),
    val error: String? = null
)