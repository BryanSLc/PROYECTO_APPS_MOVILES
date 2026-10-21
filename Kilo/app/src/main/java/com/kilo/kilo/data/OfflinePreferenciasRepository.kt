package com.kilo.kilo.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda las preferencias en el teléfono, sin internet. */
@Singleton
class OfflinePreferenciasRepository @Inject constructor(
    @ApplicationContext context: Context
) : PreferenciasRepository {

    private val prefs = context.getSharedPreferences("kilo_preferencias", Context.MODE_PRIVATE)

    private val _preferencias = MutableStateFlow(cargar())
    override val preferencias: StateFlow<Preferencias> = _preferencias.asStateFlow()

    override fun setNotificaciones(activas: Boolean) {
        guardar(_preferencias.value.copy(notificacionesActivas = activas))
    }

    override fun setTemaOscuro(oscuro: Boolean) {
        guardar(_preferencias.value.copy(temaOscuro = oscuro))
    }

    override fun marcarPermisoNotificacionesSolicitado() {
        guardar(_preferencias.value.copy(permisoNotificacionesSolicitado = true))
    }

    private fun guardar(nuevas: Preferencias) {
        _preferencias.value = nuevas
        prefs.edit()
            .putBoolean(CLAVE_NOTIFICACIONES, nuevas.notificacionesActivas)
            .putBoolean(CLAVE_TEMA_OSCURO, nuevas.temaOscuro)
            .putBoolean(CLAVE_PERMISO_SOLICITADO, nuevas.permisoNotificacionesSolicitado)
            .apply()
    }

    private fun cargar() = Preferencias(
        notificacionesActivas = prefs.getBoolean(CLAVE_NOTIFICACIONES, true),
        temaOscuro = prefs.getBoolean(CLAVE_TEMA_OSCURO, false),
        permisoNotificacionesSolicitado = prefs.getBoolean(CLAVE_PERMISO_SOLICITADO, false)
    )

    private companion object {
        const val CLAVE_NOTIFICACIONES = "notificaciones"
        const val CLAVE_TEMA_OSCURO = "tema_oscuro"
        const val CLAVE_PERMISO_SOLICITADO = "permiso_notificaciones_solicitado"
    }
}