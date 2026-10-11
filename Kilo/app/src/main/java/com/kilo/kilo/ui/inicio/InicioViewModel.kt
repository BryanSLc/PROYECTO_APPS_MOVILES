package com.kilo.kilo.ui.inicio

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class InicioViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(InicioState())
    val state: StateFlow<InicioState> = _state.asStateFlow()

    fun onActualizarKmClick() {
        _state.update { it.copy(mostrarActualizarKm = true) }
    }

    fun onActualizarKmCerrar() {
        _state.update { it.copy(mostrarActualizarKm = false) }
    }
}