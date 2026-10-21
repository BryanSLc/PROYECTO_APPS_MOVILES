package com.kilo.kilo.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

// ===================================================================
//  CÁLCULO: decide si cada pieza está vencida, próxima, al día o sin datos
//  comparando el kilometraje del vehículo contra el límite de cada pieza.
// ===================================================================

object CalculoMantenimiento {

    /** Desde este avance del intervalo (80%) la pieza pasa a "Próximo". */
    const val UMBRAL_PROXIMO = 0.80f

    fun estadoDe(
        pieza: Pieza,
        vehiculo: Vehiculo,
        hoy: LocalDate = LocalDate.now()
    ): EstadoDePieza {
        // El último mantenimiento es el más reciente (por fecha; si empatan, el último registrado)
        val ultimo = vehiculo.registros
            .filter { it.piezaId == pieza.id }
            .maxWithOrNull(compareBy<RegistroMantenimiento>({ it.fecha }, { it.id }))
            ?: return EstadoDePieza(pieza, Situacion.SIN_DATOS, null, null, null, null, 0f, false)

        val intervaloKm = pieza.intervaloKm.coerceAtLeast(1)
        val kmMeta = ultimo.km + intervaloKm
        val progresoKm = (vehiculo.km - ultimo.km).coerceAtLeast(0).toFloat() / intervaloKm

        // El tiempo solo cuenta si la pieza tiene límite en meses
        val fechaLimite = pieza.intervaloMeses?.let { ultimo.fecha.plusMonths(it.toLong()) }
        val progresoTiempo = if (fechaLimite != null) {
            val diasDelIntervalo = ChronoUnit.DAYS.between(ultimo.fecha, fechaLimite).coerceAtLeast(1)
            val diasPasados = ChronoUnit.DAYS.between(ultimo.fecha, hoy).coerceAtLeast(0)
            diasPasados.toFloat() / diasDelIntervalo
        } else {
            0f
        }

        val progreso = max(progresoKm, progresoTiempo)
        val situacion = when {
            progreso >= 1f -> Situacion.VENCIDO
            progreso >= UMBRAL_PROXIMO -> Situacion.PROXIMO
            else -> Situacion.AL_DIA
        }

        return EstadoDePieza(
            pieza = pieza,
            situacion = situacion,
            ultimo = ultimo,
            kmMeta = kmMeta,
            fechaLimite = fechaLimite,
            kmRestantes = kmMeta - vehiculo.km,
            progreso = progreso,
            limitaPorTiempo = fechaLimite != null && progresoTiempo > progresoKm
        )
    }

    fun estadosDe(vehiculo: Vehiculo, hoy: LocalDate = LocalDate.now()): List<EstadoDePieza> =
        vehiculo.piezas.map { estadoDe(it, vehiculo, hoy) }

    /** Estados del vehículo activo. */
    fun estadosDe(datos: DatosKilo, hoy: LocalDate = LocalDate.now()): List<EstadoDePieza> =
        estadosDe(datos.vehiculo, hoy)
}