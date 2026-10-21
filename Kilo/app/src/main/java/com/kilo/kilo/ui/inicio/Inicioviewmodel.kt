package com.kilo.kilo.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.CalculoMantenimiento
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.enKm
import com.kilo.kilo.ui.piezasporcategorias.EstadoPieza
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
class InicioViewModel @Inject constructor(
    repositorio: KiloRepository
) : ViewModel() {

    private val mostrarActualizarKm = MutableStateFlow(false)

    val uiState: StateFlow<UiState<InicioState>> = combine(
        repositorio.datos,
        mostrarActualizarKm
    ) { datos, mostrar -> construirEstado(datos, mostrar) }
        .comoUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun onActualizarKmClick() {
        mostrarActualizarKm.value = true
    }

    fun onActualizarKmCerrar() {
        mostrarActualizarKm.value = false
    }
}

private fun construirEstado(datos: DatosKilo, mostrar: Boolean): InicioState {
    val porEstado = CalculoMantenimiento.estadosDe(datos).groupBy { it.situacion.aEstadoPieza() }
    return InicioState(
        vehiculo = VehiculoResumen(
            nombre = datos.vehiculo.nombre,
            placa = datos.vehiculo.placa,
            carroceria = datos.vehiculo.carroceria,
            kilometraje = datos.vehiculo.km.enKm()
        ),
        resumenes = EstadoPieza.values().map { ResumenEstado(it, porEstado[it].orEmpty().size) },
        mostrarActualizarKm = mostrar
    )
}