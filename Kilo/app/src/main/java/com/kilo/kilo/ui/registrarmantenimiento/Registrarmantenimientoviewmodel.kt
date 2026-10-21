package com.kilo.kilo.ui.registrarmantenimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.FORMATO_FECHA_ENTRADA
import com.kilo.kilo.data.FORMATO_FECHA_SALIDA
import com.kilo.kilo.data.KiloRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

/** Eventos de una sola vez: la hoja se cierra al guardar. */
sealed interface RegistrarMantenimientoEvento {
    object Guardado : RegistrarMantenimientoEvento
}

@HiltViewModel
class RegistrarMantenimientoViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val _state = MutableStateFlow(estadoVacio())
    val state: StateFlow<RegistrarMantenimientoState> = _state.asStateFlow()

    private val _eventos = Channel<RegistrarMantenimientoEvento>(Channel.BUFFERED)
    val eventos: Flow<RegistrarMantenimientoEvento> = _eventos.receiveAsFlow()

    /** Al abrir la hoja: pieza elegida, fecha de hoy y el km actual del vehículo (leídos de Room). */
    fun preparar(claveTipo: String?) {
        viewModelScope.launch {
            val vehiculo = repositorio.datos.first().vehiculo
            val pieza = vehiculo.piezas.firstOrNull { it.id.toString() == claveTipo }
                ?: vehiculo.piezas.first()
            _state.value = RegistrarMantenimientoState(
                tipos = vehiculo.piezas.map { it.nombre },
                tipoSeleccionado = pieza.nombre,
                fecha = LocalDate.now().format(FORMATO_FECHA_SALIDA),
                kmDelCambio = vehiculo.km.toString()
            )
        }
    }

    fun onTipoChange(tipo: String) {
        _state.update { it.copy(tipoSeleccionado = tipo, error = null) }
    }

    fun onFechaChange(fecha: String) {
        _state.update { it.copy(fecha = fecha, error = null) }
    }

    fun onKmChange(km: String) {
        _state.update { it.copy(kmDelCambio = km.filter(Char::isDigit).take(7), error = null) }
    }

    fun onNotasChange(notas: String) {
        _state.update { it.copy(notas = notas) }
    }

    /** Valida y guarda en Room; si sale bien avisa con un evento para cerrar la hoja. */
    fun onGuardar() {
        val actual = _state.value

        val fecha = try {
            LocalDate.parse(actual.fecha.trim(), FORMATO_FECHA_ENTRADA)
        } catch (e: DateTimeParseException) {
            return fallo("Escribe la fecha como día/mes/año, por ejemplo 13/09/2026")
        }

        val kmIngresado = actual.kmDelCambio.toIntOrNull()
            ?: return fallo("Escribe el kilometraje del cambio")

        viewModelScope.launch {
            val vehiculo = repositorio.datos.first().vehiculo
            val pieza = vehiculo.piezas.firstOrNull { it.nombre == actual.tipoSeleccionado }
            if (pieza == null) {
                fallo("Elige qué se cambió")
                return@launch
            }

            val error = repositorio.registrarMantenimiento(pieza.id, fecha, kmIngresado, actual.notas)
            if (error != null) {
                fallo(error)
                return@launch
            }

            _state.value = estadoVacio()
            _eventos.send(RegistrarMantenimientoEvento.Guardado)
        }
    }

    fun onCancelar() {
        _state.value = estadoVacio()
    }

    private fun fallo(mensaje: String) {
        _state.update { it.copy(error = mensaje) }
    }

    /** Estado sin datos del vehículo; `preparar()` lo completa cada vez que se abre la hoja. */
    private fun estadoVacio() = RegistrarMantenimientoState(
        tipos = emptyList(),
        tipoSeleccionado = "",
        fecha = "",
        kmDelCambio = ""
    )
}
