package com.kilo.kilo.ui.actualizar_km

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.ui.common.UiState
import com.kilo.kilo.ui.common.comoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Eventos de una sola vez (no son estado): la hoja se cierra al guardar. */
sealed interface ActualizarKmEvento {
    object Guardado : ActualizarKmEvento
}

@HiltViewModel
class ActualizarKmViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val kmNuevo = MutableStateFlow("")
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<ActualizarKmState>> = combine(
        repositorio.datos,
        kmNuevo,
        error
    ) { datos, nuevo, mensaje ->
        ActualizarKmState(
            vehiculo = datos.vehiculo.nombre,
            kmActual = datos.vehiculo.km,
            kmNuevo = nuevo,
            error = mensaje
        )
    }
        .comoUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    private val _eventos = Channel<ActualizarKmEvento>(Channel.BUFFERED)
    val eventos: Flow<ActualizarKmEvento> = _eventos.receiveAsFlow()

    fun onKmChange(valor: String) {
        kmNuevo.value = valor.filter { c -> c.isDigit() }.take(7)
        error.value = null
    }

    /** Guarda el km en Room; si es válido avisa con un evento para cerrar la hoja. */
    fun onConfirmar() {
        val nuevo = kmNuevo.value.toIntOrNull() ?: return
        viewModelScope.launch {
            val mensaje = repositorio.actualizarKm(nuevo)
            if (mensaje != null) {
                error.value = mensaje
                return@launch
            }
            kmNuevo.value = ""
            _eventos.send(ActualizarKmEvento.Guardado)
        }
    }

    fun onCancelar() {
        kmNuevo.value = ""
        error.value = null
    }
}
