package com.kilo.kilo.ui.agregar_vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.NuevaPieza
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Eventos de una sola vez: la pantalla vuelve atrás al guardar. */
sealed interface AgregarVehiculoEvento {
    object Guardado : AgregarVehiculoEvento
}

@HiltViewModel
class AgregarVehiculoViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AgregarVehiculoState())
    val state: StateFlow<AgregarVehiculoState> = _state.asStateFlow()

    private val _eventos = Channel<AgregarVehiculoEvento>(Channel.BUFFERED)
    val eventos: Flow<AgregarVehiculoEvento> = _eventos.receiveAsFlow()

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
        _state.update { it.copy(kilometraje = valor.filter { c -> c.isDigit() }.take(7), error = null) }
    }

    fun onTipoChange(tipo: String) {
        _state.update { it.copy(tipoSeleccionado = tipo) }
    }

    // ------------------------------------------------------------ piezas

    fun onPiezaNombreChange(id: Long, valor: String) = editarPieza(id) { it.copy(nombre = valor) }

    fun onPiezaLimiteChange(id: Long, valor: String) =
        editarPieza(id) { it.copy(limiteKm = valor.filter { c -> c.isDigit() }.take(7)) }

    fun onPiezaMesesChange(id: Long, valor: String) =
        editarPieza(id) { it.copy(meses = valor.filter { c -> c.isDigit() }.take(3)) }

    fun onPiezaUltimoKmChange(id: Long, valor: String) =
        editarPieza(id) { it.copy(ultimoKm = valor.filter { c -> c.isDigit() }.take(7)) }

    fun onAgregarPieza() {
        _state.update { actual ->
            val siguiente = (actual.piezas.maxOfOrNull { it.id } ?: 0L) + 1
            actual.copy(piezas = actual.piezas + PiezaForm(id = siguiente), error = null)
        }
    }

    fun onQuitarPieza(id: Long) {
        _state.update { actual ->
            actual.copy(piezas = actual.piezas.filterNot { it.id == id }, error = null)
        }
    }

    private fun editarPieza(id: Long, cambio: (PiezaForm) -> PiezaForm) {
        _state.update { actual ->
            actual.copy(
                piezas = actual.piezas.map { if (it.id == id) cambio(it) else it },
                error = null
            )
        }
    }

    // ------------------------------------------------------------ guardar

    /** Valida y guarda en Room; si sale bien avisa con un evento para volver atrás. */
    fun onGuardar() {
        val actual = _state.value
        if (!actual.puedeGuardar) return fallo("Completa marca, modelo, placa y kilometraje")

        val km = actual.kilometraje.toIntOrNull() ?: return fallo("El kilometraje no es válido")

        val piezas = mutableListOf<NuevaPieza>()
        for (p in actual.piezas) {
            val nombre = p.nombre.trim()
            if (nombre.isEmpty()) return fallo("Todas las piezas necesitan un nombre")

            val limite = p.limiteKm.toIntOrNull()
            if (limite == null || limite <= 0) {
                return fallo("\"$nombre\": escribe cada cuántos km se cambia")
            }

            val meses = p.meses.toIntOrNull()
            if (p.meses.isNotBlank() && (meses == null || meses <= 0)) {
                return fallo("\"$nombre\": los meses deben ser mayores a 0")
            }

            val ultimo = p.ultimoKm.toIntOrNull()
            if (p.ultimoKm.isNotBlank() && ultimo == null) {
                return fallo("\"$nombre\": el km del último cambio no es válido")
            }

            piezas += NuevaPieza(nombre, limite, meses, ultimo)
        }

        val marca = actual.marca.trim()
        val modelo = actual.modelo.trim()
        val base = if (modelo.startsWith(marca, ignoreCase = true)) modelo else "$marca $modelo"
        val nombre = listOf(base, actual.anio.trim()).filter { it.isNotBlank() }.joinToString(" ")

        viewModelScope.launch {
            val error = repositorio.agregarVehiculo(
                nombre = nombre,
                placa = actual.placa.trim(),
                carroceria = actual.tipoSeleccionado,
                km = km,
                piezas = piezas
            )
            if (error != null) {
                fallo(error)
                return@launch
            }
            _state.value = AgregarVehiculoState()
            _eventos.send(AgregarVehiculoEvento.Guardado)
        }
    }

    private fun fallo(mensaje: String) {
        _state.update { it.copy(error = mensaje) }
    }
}
