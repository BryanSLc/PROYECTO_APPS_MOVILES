package com.kilo.kilo.data

import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val KM_POR_MILLA = 1.609344

/**
 * Unidad con la que el usuario ve y escribe las distancias.
 *
 * Room SIEMPRE guarda kilómetros (única fuente de verdad). La conversión a millas
 * solo ocurre en el borde: al mostrar un valor ([desdeKm]) y al leer lo que escribe el usuario ([aKm]).
 */
enum class UnidadDistancia(val clave: String) {
    KM("km"),
    MI("mi");

    /** Texto corto que se muestra junto al número ("km" o "mi"). */
    val simbolo: String get() = clave

    /** De kilómetros (lo guardado) a la unidad elegida, redondeado. */
    fun desdeKm(km: Int): Int = if (this == MI) (km / KM_POR_MILLA).roundToInt() else km

    /** De lo que escribió el usuario (en la unidad elegida) a kilómetros para guardar. */
    fun aKm(valor: Int): Int = if (this == MI) (valor * KM_POR_MILLA).roundToInt() else valor

    /**
     * Igual que [aKm], pero si el usuario escribe exactamente lo que se le mostraba para
     * [referenciaKm] (por ejemplo, el km actual), devuelve esa referencia sin perder precisión
     * por el redondeo de ida y vuelta.
     */
    fun aKmCercano(valor: Int, referenciaKm: Int): Int =
        if (desdeKm(referenciaKm) == valor) referenciaKm else aKm(valor)

    /** "68,450 km" o "42,533 mi" a partir de kilómetros. */
    fun formato(km: Int): String = "${desdeKm(km).conComas()} $simbolo"

    companion object {
        fun deClave(clave: String?): UnidadDistancia = if (clave == MI.clave) MI else KM
    }
}

/** La unidad elegida en Configuración, como flujo (se vuelve a emitir al cambiarla). */
val PreferenciasRepository.unidad: Flow<UnidadDistancia>
    get() = preferencias.map { UnidadDistancia.deClave(it.unidadDistancia) }.distinctUntilChanged()

/** La unidad elegida en este momento. */
val PreferenciasRepository.unidadActual: UnidadDistancia
    get() = UnidadDistancia.deClave(preferencias.value.unidadDistancia)
