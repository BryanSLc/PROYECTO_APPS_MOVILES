package com.kilo.kilo.ui.configuracion

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConfiguracionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ConfiguracionState())
    val state: StateFlow<ConfiguracionState> = _state.asStateFlow()

    fun onNotificacionesChange(activas: Boolean) {
        _state.update { it.copy(notificacionesActivas = activas) }
    }

    fun onUnidadChange(unidad: String) {
        _state.update { it.copy(unidadDistancia = unidad) }
    }
}