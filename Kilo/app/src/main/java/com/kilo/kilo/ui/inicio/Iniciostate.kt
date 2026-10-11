package com.kilo.kilo.ui.inicio

import com.kilo.kilo.ui.piezasporcategorias.EstadoPieza
import com.kilo.kilo.ui.piezasporcategorias.piezasDeEjemplo

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
    val nombreUsuario: String = "Andrea",
    val vehiculo: VehiculoResumen = VehiculoResumen(
        nombre = "Mazda 3 2019",
        placa = "PBX-482",
        carroceria = "Sedán",
        kilometraje = "68,450 km"
    ),
    // Los conteos salen de los mismos datos de ejemplo de PiezasPorCategoriasState
    val resumenes: List<ResumenEstado> = EstadoPieza.values().map { estado ->
        ResumenEstado(estado, piezasDeEjemplo[estado].orEmpty().size)
    },
    // Se pondrá en true al tocar "Actualizar km" (la hoja 06 aún no está hecha)
    val mostrarActualizarKm: Boolean = false
)