package com.kilo.kilo.ui.piezasporcategorias

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PiezasPorCategoriasViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PiezasPorCategoriasState())
    val state: StateFlow<PiezasPorCategoriasState> = _state.asStateFlow()


    fun onTarjetaClick(estado: EstadoPieza) {
        _state.update { it.copy(estadoAbierto = estado) }
    }

    fun onCerrar() {
        _state.update { it.copy(estadoAbierto = null) }
    }
}