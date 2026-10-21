package com.kilo.kilo.ui.inicio

import com.kilo.kilo.ui.piezasporcategorias.EstadoPieza

data class VehiculoResumen(
    val nombre: String,
    val placa: String,
    val carroceria: String,
    val kilometraje: String
)

/** Una tarjeta del dashboard: el estado (Vencidos, Próximos...) y cuántas piezas tiene. */
data class ResumenEstado(
    val estado: EstadoPieza,
    val cantidad: Int
)

data class InicioState(
    // Los valores por defecto solo sirven para el @Preview; en la app salen del repositorio
    val vehiculo: VehiculoResumen = VehiculoResumen(
        nombre = "Mazda 3 2019",
        placa = "PBX-482",
        carroceria = "Sedán",
        kilometraje = "68,450 km"
    ),
    val resumenes: List<ResumenEstado> = listOf(
        ResumenEstado(EstadoPieza.VENCIDOS, 1),
        ResumenEstado(EstadoPieza.PROXIMOS, 1),
        ResumenEstado(EstadoPieza.AL_DIA, 2),
        ResumenEstado(EstadoPieza.SIN_DATOS, 1)
    ),
    val mostrarActualizarKm: Boolean = false
)