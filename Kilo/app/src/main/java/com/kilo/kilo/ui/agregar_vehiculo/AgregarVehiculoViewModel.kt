package com.kilo.kilo.ui.agregar_vehiculo

import androidx.lifecycle.ViewModel
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.NuevaPieza
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AgregarVehiculoViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

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

    /** true si se guardó (la pantalla vuelve atrás); false si hay algo que corregir. */
    fun onGuardar(): Boolean {
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

        val error = repositorio.agregarVehiculo(
            nombre = nombre,
            placa = actual.placa.trim(),
            carroceria = actual.tipoSeleccionado,
            km = km,
            piezas = piezas
        )
        if (error != null) return fallo(error)

        _state.value = AgregarVehiculoState()
        return true
    }

    private fun fallo(mensaje: String): Boolean {
        _state.update { it.copy(error = mensaje) }
        return false
    }
}
