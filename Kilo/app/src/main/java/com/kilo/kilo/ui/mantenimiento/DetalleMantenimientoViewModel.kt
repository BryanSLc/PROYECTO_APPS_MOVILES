package com.kilo.kilo.ui.mantenimiento

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.CalculoMantenimiento
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.RegistroMantenimiento
import com.kilo.kilo.data.enKm
import com.kilo.kilo.data.formatoCorto
import com.kilo.kilo.ui.piezasporcategorias.aEstadoPieza
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kilo.kilo.ui.common.UiState
import com.kilo.kilo.ui.common.comoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetalleMantenimientoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repositorio: KiloRepository
) : ViewModel() {

    /** El id de la pieza llega como argumento de la ruta: detalle_mantenimiento/{tipo}. */
    private val piezaId: Long? = savedStateHandle.get<String>("tipo")?.toLongOrNull()

    private val mostrarRegistro = MutableStateFlow(false)

    val uiState: StateFlow<UiState<DetalleMantenimientoState>> = combine(
        repositorio.datos,
        mostrarRegistro
    ) { datos, mostrar -> construirEstado(piezaId, datos, mostrar) }
        .comoUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    /** "Marcar como realizado" abre la hoja Registrar mantenimiento. */
    fun onMarcarRealizado() {
        mostrarRegistro.value = true
    }

    fun onCerrarRegistro() {
        mostrarRegistro.value = false
    }
}

private fun construirEstado(
    piezaId: Long?,
    datos: DatosKilo,
    mostrarRegistro: Boolean
): DetalleMantenimientoState {
    val vehiculo = datos.vehiculo
    val pieza = vehiculo.piezas.firstOrNull { it.id == piezaId } ?: vehiculo.piezas.first()
    val e = CalculoMantenimiento.estadoDe(pieza, vehiculo)
    val km = { valor: Int? -> valor?.enKm() ?: "—" }

    val historial = vehiculo.registros
        .filter { it.piezaId == pieza.id }
        .sortedWith(
            compareByDescending<RegistroMantenimiento> { it.km }.thenByDescending { it.fecha }
        )
        .map { r ->
            RegistroDetalle(
                km = km(r.km),
                detalle = listOfNotNull(
                    r.fecha.formatoCorto(),
                    r.nota.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
            )
        }

    return DetalleMantenimientoState(
        titulo = pieza.nombre,
        intervalo = pieza.descripcionIntervalo(),
        estado = e.situacion.aEstadoPieza(),
        etiquetaEstado = e.situacion.etiqueta,
        progreso = e.progreso.coerceIn(0f, 1f),
        ultimoKm = km(e.ultimo?.km),
        metaKm = km(e.kmMeta),
        proximoCambio = km(e.kmMeta),
        fechaLimite = e.fechaLimite?.formatoCorto() ?: "—",
        historial = historial,
        mostrarRegistro = mostrarRegistro,
        tipoClave = pieza.id.toString()
    )
}