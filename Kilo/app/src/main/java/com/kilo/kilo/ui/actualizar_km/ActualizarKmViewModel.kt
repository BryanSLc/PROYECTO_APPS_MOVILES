package com.kilo.kilo.ui.actualizar_km

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ActualizarKmViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ActualizarKmState())
    val state: StateFlow<ActualizarKmState> = _state.asStateFlow()

    fun onKmChange(valor: String) {
        _state.update { it.copy(kmNuevo = valor.filter { c -> c.isDigit() }.take(7)) }
    }

    fun onConfirmar() {
        // Cuando exista el repositorio, aquí se guarda el nuevo kilometraje.
        _state.update { it.copy(kmNuevo = "") }
    }

    fun onCancelar() {
        _state.update { it.copy(kmNuevo = "") }
    }
}