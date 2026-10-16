package com.kilo.kilo.ui.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    val state: StateFlow<ConfiguracionState> = preferencias.preferencias
        .map { ConfiguracionState(it.notificacionesActivas, it.unidadDistancia) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = preferencias.preferencias.value.let {
                ConfiguracionState(it.notificacionesActivas, it.unidadDistancia)
            }
        )

    fun onNotificacionesChange(activas: Boolean) {
        preferencias.setNotificaciones(activas)
    }

    fun onUnidadChange(unidad: String) {
        preferencias.setUnidadDistancia(unidad)
    }
}