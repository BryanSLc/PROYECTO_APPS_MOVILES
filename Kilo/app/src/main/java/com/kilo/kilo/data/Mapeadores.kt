package com.kilo.kilo.data

import com.kilo.kilo.data.local.VehiculoConDetalle

// ===================================================================
//  MAPEADORES: convierten las entidades de Room en modelos de dominio.
//  Así Room queda oculto detrás del repositorio.
// ===================================================================

fun VehiculoConDetalle.aDominio(): Vehiculo = Vehiculo(
    id = vehiculo.id,
    nombre = vehiculo.nombre,
    placa = vehiculo.placa,
    carroceria = vehiculo.carroceria,
    km = vehiculo.km,
    piezas = piezas.sortedBy { it.id }.map {
        Pieza(it.id, it.nombre, it.intervaloKm, it.intervaloMeses)
    },
    registros = registros.sortedBy { it.id }.map {
        RegistroMantenimiento(it.id, it.piezaId, it.fecha, it.km, it.nota)
    }
)

/** Requiere una lista NO vacía (el repositorio garantiza que siempre hay un vehículo). */
fun List<VehiculoConDetalle>.aDatosKilo(): DatosKilo {
    val vehiculos = map { it.aDominio() }
    val activoId = firstOrNull { it.vehiculo.activo }?.vehiculo?.id ?: vehiculos.first().id
    return DatosKilo(vehiculos, activoId)
}
