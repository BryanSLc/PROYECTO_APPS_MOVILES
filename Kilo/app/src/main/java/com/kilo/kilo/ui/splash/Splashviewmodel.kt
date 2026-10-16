    package com.kilo.kilo.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        cargar()
    }


    private fun cargar() {
        viewModelScope.launch {
            repeat(10) {
                delay(150)
                _state.update { actual ->
                    actual.copy(progreso = (actual.progreso + 0.1f).coerceAtMost(1f))
                }
            }
            _state.update { actual -> actual.copy(terminado = true) }
        }
    }
}