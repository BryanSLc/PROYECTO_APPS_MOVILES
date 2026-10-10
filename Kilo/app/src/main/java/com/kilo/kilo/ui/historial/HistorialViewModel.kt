package com.kilo.kilo.ui.historial

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HistorialViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HistorialState())
    val state: StateFlow<HistorialState> = _state.asStateFlow()

    fun onFiltroChange(filtro: String) {
        _state.update { it.copy(filtroSeleccionado = filtro) }
    }

    fun onEliminar(id: Int) {
        _state.update { actual ->
            actual.copy(registros = actual.registros.filterNot { it.id == id })
        }
    }
}