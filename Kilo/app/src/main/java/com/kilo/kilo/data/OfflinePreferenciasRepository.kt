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

    override fun setUnidadDistancia(unidad: String) {
        if (unidad != "km" && unidad != "mi") return
        guardar(_preferencias.value.copy(unidadDistancia = unidad))
    }

    private fun guardar(nuevas: Preferencias) {
        _preferencias.value = nuevas
        prefs.edit()
            .putBoolean(CLAVE_NOTIFICACIONES, nuevas.notificacionesActivas)
            .putString(CLAVE_UNIDAD, nuevas.unidadDistancia)
            .apply()
    }

    private fun cargar() = Preferencias(
        notificacionesActivas = prefs.getBoolean(CLAVE_NOTIFICACIONES, true),
        unidadDistancia = prefs.getString(CLAVE_UNIDAD, "km") ?: "km"
    )

    private companion object {
        const val CLAVE_NOTIFICACIONES = "notificaciones"
        const val CLAVE_UNIDAD = "unidad"
    }
}