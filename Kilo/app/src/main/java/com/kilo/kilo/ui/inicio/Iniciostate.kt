package com.kilo.kilo.ui.inicio

data class VehiculoResumen(
    val nombre: String,
    val placa: String,
    val carroceria: String,
    val kilometraje: String
)


data class ResumenEstado(
    val estado: EstadoPieza,
    val cantidad: Int
)

data class InicioState(
    val nombreUsuario: String = "Andrea",
    val vehiculo: VehiculoResumen = VehiculoResumen(
        nombre = "Mazda 3 2019",
        placa = "PBX-482",
        carroceria = "Sedán",
        kilometraje = "68,450 km"
    ),

    val resumenes: List<ResumenEstado> = EstadoPieza.values().map { estado ->
        ResumenEstado(estado, piezasDeEjemplo[estado].orEmpty().size)
    },

    val mostrarActualizarKm: Boolean = false
)