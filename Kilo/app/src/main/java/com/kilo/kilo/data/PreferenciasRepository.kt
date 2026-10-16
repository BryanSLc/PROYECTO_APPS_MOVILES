package com.kilo.kilo.data

import kotlinx.coroutines.flow.StateFlow

/** Preferencias del usuario (pantalla Configuración). */
data class Preferencias(
    val notificacionesActivas: Boolean = true,
    /** "km" o "mi" */
    val unidadDistancia: String = "km"
)

interface PreferenciasRepository {
    val preferencias: StateFlow<Preferencias>

    fun setNotificaciones(activas: Boolean)

    fun setUnidadDistancia(unidad: String)
}