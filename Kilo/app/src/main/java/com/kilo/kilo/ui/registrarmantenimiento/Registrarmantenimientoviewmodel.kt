package com.kilo.kilo.ui.registrarmantenimiento

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegistrarMantenimientoViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(RegistrarMantenimientoState())
    val state: StateFlow<RegistrarMantenimientoState> = _state.asStateFlow()

    fun onTipoChange(tipo: String) {
        _state.update { it.copy(tipoSeleccionado = tipo) }
    }

    fun onFechaChange(fecha: String) {
        _state.update { it.copy(fecha = fecha) }
    }

    fun onKmChange(km: String) {
        _state.update { it.copy(kmDelCambio = km.filter(Char::isDigit)) }
    }

    fun onNotasChange(notas: String) {
        _state.update { it.copy(notas = notas) }
    }

    fun onGuardar() {

        _state.value = RegistrarMantenimientoState()
    }

    fun onCancelar() {
        _state.value = RegistrarMantenimientoState()
    }
}