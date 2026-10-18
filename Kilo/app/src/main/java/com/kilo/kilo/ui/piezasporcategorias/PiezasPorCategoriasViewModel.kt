package com.kilo.kilo.ui.piezasporcategorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.CalculoMantenimiento
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.KiloRepository
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
class PiezasPorCategoriasViewModel @Inject constructor(
    repositorio: KiloRepository
) : ViewModel() {

    private val estadoAbierto = MutableStateFlow<EstadoPieza?>(null)

    val uiState: StateFlow<UiState<PiezasPorCategoriasState>> = combine(
        repositorio.datos,
        estadoAbierto
    ) { datos, abierto -> PiezasPorCategoriasState(abierto, agrupar(datos)) }
        .comoUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun onTarjetaClick(estado: EstadoPieza) {
        estadoAbierto.value = estado
    }

    fun onCerrar() {
        estadoAbierto.value = null
    }
}

private fun agrupar(datos: DatosKilo): Map<EstadoPieza, List<PiezaResumen>> {
    val porEstado = CalculoMantenimiento.estadosDe(datos).groupBy { it.situacion.aEstadoPieza() }
    return EstadoPieza.values().associateWith { estado ->
        porEstado[estado].orEmpty().map { PiezaResumen(it.pieza.nombre, it.detalle, it.pieza.id.toString()) }
    }
}