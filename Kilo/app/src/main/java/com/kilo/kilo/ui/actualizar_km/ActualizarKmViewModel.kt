package com.kilo.kilo.ui.actualizar_km

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.KiloRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActualizarKmViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val kmNuevo = MutableStateFlow("")
    private val error = MutableStateFlow<String?>(null)

    val state: StateFlow<ActualizarKmState> = combine(
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActualizarKmState(
            vehiculo = repositorio.datos.value.vehiculo.nombre,
            kmActual = repositorio.datos.value.vehiculo.km
        )
    )

    fun onKmChange(valor: String) {
        kmNuevo.value = valor.filter { c -> c.isDigit() }.take(7)
        error.value = null
    }

    /** true si se guardó (la hoja se cierra); false si el km no es válido. */
    fun onConfirmar(): Boolean {
        val nuevo = kmNuevo.value.toIntOrNull() ?: return false
        val mensaje = repositorio.actualizarKm(nuevo)
        if (mensaje != null) {
            error.value = mensaje
            return false
        }
        kmNuevo.value = ""
        return true
    }

    fun onCancelar() {
        kmNuevo.value = ""
        error.value = null
    }
}