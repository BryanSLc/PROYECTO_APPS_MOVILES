package com.kilo.kilo.ui.mis_vehiculos

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MisVehiculosViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(MisVehiculosState())
    val state: StateFlow<MisVehiculosState> = _state.asStateFlow()

    fun onEliminar(id: Int) {
        _state.update { actual ->
            actual.copy(vehiculos = actual.vehiculos.filterNot { it.id == id })
        }
    }
}