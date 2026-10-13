package com.kilo.kilo.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max


object CalculoMantenimiento {

    const val UMBRAL_PROXIMO = 0.80f

    fun estadoDe(
        tipo: TipoPieza,
        datos: DatosKilo,
        hoy: LocalDate = LocalDate.now()
    ): EstadoDePieza {
        val ultimo = datos.registros
            .filter { it.tipo == tipo }
            .maxWithOrNull(compareBy<RegistroMantenimiento>({ it.km }, { it.fecha }))
            ?: return EstadoDePieza(tipo, Situacion.SIN_DATOS, null, null, null, null, 0f, false)

        val kmActual = datos.vehiculo.km
        val kmMeta = ultimo.km + tipo.intervaloKm
        val fechaLimite = ultimo.fecha.plusMonths(tipo.intervaloMeses.toLong())

        val progresoKm = (kmActual - ultimo.km).coerceAtLeast(0).toFloat() / tipo.intervaloKm

        val diasDelIntervalo = ChronoUnit.DAYS.between(ultimo.fecha, fechaLimite).coerceAtLeast(1)
        val diasPasados = ChronoUnit.DAYS.between(ultimo.fecha, hoy).coerceAtLeast(0)
        val progresoTiempo = diasPasados.toFloat() / diasDelIntervalo

        val progreso = max(progresoKm, progresoTiempo)
        val situacion = when {
            progreso >= 1f -> Situacion.VENCIDO
            progreso >= UMBRAL_PROXIMO -> Situacion.PROXIMO
            else -> Situacion.AL_DIA
        }

        return EstadoDePieza(
            tipo = tipo,
            situacion = situacion,
            ultimo = ultimo,
            kmMeta = kmMeta,
            fechaLimite = fechaLimite,
            kmRestantes = kmMeta - kmActual,
            progreso = progreso,
            limitaPorTiempo = progresoTiempo > progresoKm
        )
    }

    fun estadosDe(datos: DatosKilo, hoy: LocalDate = LocalDate.now()): List<EstadoDePieza> =
        TipoPieza.values().map { estadoDe(it, datos, hoy) }
}