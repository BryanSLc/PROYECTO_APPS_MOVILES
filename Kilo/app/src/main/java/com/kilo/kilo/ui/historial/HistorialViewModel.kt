package com.kilo.kilo.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.RegistroMantenimiento
import com.kilo.kilo.data.conComas
import com.kilo.kilo.data.formatoCorto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val filtro = MutableStateFlow(FILTRO_TODOS)
    private val error = MutableStateFlow<String?>(null)

    val state: StateFlow<HistorialState> = combine(
        repositorio.datos,
        filtro,
        error
    ) { datos, filtroElegido, mensaje -> construirEstado(datos, filtroElegido, mensaje) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = construirEstado(repositorio.datos.value, FILTRO_TODOS, null)
        )

    fun onFiltroChange(nuevo: String) {
        filtro.value = nuevo
    }

    fun onEliminar(id: Long) {
        error.value = repositorio.eliminarRegistro(id)
    }
}

private fun construirEstado(datos: DatosKilo, filtroElegido: String, error: String?): HistorialState {
    val vehiculo = datos.vehiculo
    val nombres = vehiculo.piezas.associate { it.id to it.nombre }
    val filtros = listOf(FILTRO_TODOS) + vehiculo.piezas.map { it.nombre }

    val registros = vehiculo.registros
        .sortedWith(
            compareByDescending<RegistroMantenimiento> { it.fecha }.thenByDescending { it.id }
        )
        .map { r ->
            val nombre = nombres[r.piezaId] ?: "Pieza eliminada"
            RegistroHistorial(
                id = r.id,
                tipo = nombre,
                abreviatura = nombre.take(3).uppercase(),
                titulo = nombre,
                fecha = r.fecha.formatoCorto(),
                kilometraje = "${r.km.conComas()} km",
                nota = r.nota
            )
        }

    return HistorialState(
        vehiculo = vehiculo.nombre,
        filtros = filtros,
        filtroSeleccionado = if (filtroElegido in filtros) filtroElegido else FILTRO_TODOS,
        registros = registros,
        error = error
    )
}