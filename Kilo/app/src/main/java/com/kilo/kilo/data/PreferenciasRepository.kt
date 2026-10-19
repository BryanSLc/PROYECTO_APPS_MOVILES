package com.kilo.kilo.data

import kotlinx.coroutines.flow.StateFlow

/** Preferencias del usuario (pantalla Configuración). */
data class Preferencias(
    /** Avisa cuando una pieza pasa a "Próximo" (a punto de vencerse). */
    val notificacionesActivas: Boolean = true,
    /** true = tema oscuro, false = tema claro. */
    val temaOscuro: Boolean = false,
    /** Para pedir el permiso de notificaciones una sola vez al abrir la app. */
    val permisoNotificacionesSolicitado: Boolean = false
)

interface PreferenciasRepository {
    val preferencias: StateFlow<Preferencias>

    fun setNotificaciones(activas: Boolean)

    fun setTemaOscuro(oscuro: Boolean)

    fun marcarPermisoNotificacionesSolicitado()
}