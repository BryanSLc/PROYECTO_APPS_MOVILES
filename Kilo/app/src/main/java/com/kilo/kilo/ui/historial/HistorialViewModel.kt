package com.kilo.kilo.ui.historial

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HistorialViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HistorialState())
    val state: StateFlow<HistorialState> = _state.asStateFlow()

    // Aquí puedes agregar los métodos para cargar los datos del historial más adelante
}