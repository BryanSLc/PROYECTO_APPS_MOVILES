package com.kilo.kilo.ui.registrarmantenimiento

import androidx.lifecycle.ViewModel
import com.kilo.kilo.data.FORMATO_FECHA_ENTRADA
import com.kilo.kilo.data.FORMATO_FECHA_SALIDA
import com.kilo.kilo.data.KiloRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class RegistrarMantenimientoViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val _state = MutableStateFlow(estadoInicial(null))
    val state: StateFlow<RegistrarMantenimientoState> = _state.asStateFlow()

    /** Al abrir la hoja: pieza elegida, fecha de hoy y el km actual del vehículo. */
    fun preparar(claveTipo: String?) {
        _state.value = estadoInicial(claveTipo)
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

    /** Devuelve true si se guardó (la pantalla cierra la hoja); false si hay algo que corregir. */
    fun onGuardar(): Boolean {
        val actual = _state.value

        val pieza = repositorio.datos.value.vehiculo.piezas
            .firstOrNull { it.nombre == actual.tipoSeleccionado }
            ?: return fallo("Elige qué se cambió")

        val fecha = try {
            LocalDate.parse(actual.fecha.trim(), FORMATO_FECHA_ENTRADA)
        } catch (e: DateTimeParseException) {
            return fallo("Escribe la fecha como día/mes/año, por ejemplo 13/09/2026")
        }

        val km = actual.kmDelCambio.toIntOrNull()
            ?: return fallo("Escribe el kilometraje del cambio")

        val error = repositorio.registrarMantenimiento(pieza.id, fecha, km, actual.notas)
        if (error != null) return fallo(error)

        _state.value = estadoInicial(null)
        return true
    }

    fun onCancelar() {
        _state.value = estadoInicial(null)
    }

    private fun fallo(mensaje: String): Boolean {
        _state.update { it.copy(error = mensaje) }
        return false
    }

    private fun estadoInicial(claveTipo: String?): RegistrarMantenimientoState {
        val vehiculo = repositorio.datos.value.vehiculo
        val pieza = vehiculo.piezas.firstOrNull { it.id.toString() == claveTipo }
            ?: vehiculo.piezas.first()
        return RegistrarMantenimientoState(
            tipos = vehiculo.piezas.map { it.nombre },
            tipoSeleccionado = pieza.nombre,
            fecha = LocalDate.now().format(FORMATO_FECHA_SALIDA),
            kmDelCambio = vehiculo.km.toString()
        )
    }
}