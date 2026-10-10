package com.kilo.kilo.ui.agregar_vehiculo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AgregarVehiculoViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AgregarVehiculoState())
    val state: StateFlow<AgregarVehiculoState> = _state.asStateFlow()

    fun onMarcaChange(valor: String) {
        _state.update { it.copy(marca = valor, error = null) }
    }

    fun onModeloChange(valor: String) {
        _state.update { it.copy(modelo = valor, error = null) }
    }

    fun onAnioChange(valor: String) {
        _state.update { it.copy(anio = valor.filter { c -> c.isDigit() }.take(4)) }
    }

    fun onPlacaChange(valor: String) {
        _state.update { it.copy(placa = valor.uppercase(), error = null) }
    }

    fun onKilometrajeChange(valor: String) {
        _state.update { it.copy(kilometraje = valor.filter { c -> c.isDigit() }, error = null) }
    }

    fun onTipoChange(tipo: String) {
        _state.update { it.copy(tipoSeleccionado = tipo) }
    }

    fun onMantenimientoKmChange(nombre: String, km: String) {
        _state.update { actual ->
            actual.copy(
                mantenimientos = actual.mantenimientos.map { m ->
                    if (m.nombre == nombre) m.copy(km = km.filter { c -> c.isDigit() }) else m
                }
            )
        }
    }

    fun onGuardar() {
        if (!_state.value.puedeGuardar) {
            _state.update { it.copy(error = "Completa marca, modelo, placa y kilometraje") }
            return
        }
        // Cuando exista el repositorio, aquí se guarda el vehículo.
    }
}