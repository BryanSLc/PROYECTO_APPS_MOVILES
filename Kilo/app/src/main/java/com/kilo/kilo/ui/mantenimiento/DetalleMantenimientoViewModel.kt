package com.kilo.kilo.ui.mantenimiento

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DetalleMantenimientoViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DetalleMantenimientoState())
    val state: StateFlow<DetalleMantenimientoState> = _state.asStateFlow()

    /** "Marcar como realizado" abre la hoja Registrar mantenimiento. */
    fun onMarcarRealizado() {
        _state.update { it.copy(mostrarRegistro = true) }
    }

    fun onCerrarRegistro() {
        _state.update { it.copy(mostrarRegistro = false) }
    }
}