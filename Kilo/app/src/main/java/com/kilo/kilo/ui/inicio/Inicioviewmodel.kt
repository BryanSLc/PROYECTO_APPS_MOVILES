package com.kilo.kilo.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.CalculoMantenimiento
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.conComas
import com.kilo.kilo.ui.piezasporcategorias.EstadoPieza
import com.kilo.kilo.ui.piezasporcategorias.aEstadoPieza
import dagger.hilt.android.lifecycle.HiltViewModel
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

    /** Se recalcula solo cada vez que cambia el kilometraje o se registra un mantenimiento. */
    val state: StateFlow<InicioState> = combine(
        repositorio.datos,
        mostrarActualizarKm
    ) { datos, mostrar -> construirEstado(datos, mostrar) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = construirEstado(repositorio.datos.value, false)
        )

    fun onActualizarKmClick() {
        mostrarActualizarKm.value = true
    }

    fun onActualizarKmCerrar() {
        mostrarActualizarKm.value = false
    }
}

private fun construirEstado(datos: DatosKilo, mostrarActualizarKm: Boolean): InicioState {
    val estados = CalculoMantenimiento.estadosDe(datos)
    return InicioState(
        vehiculo = VehiculoResumen(
            nombre = datos.vehiculo.nombre,
            placa = datos.vehiculo.placa,
            carroceria = datos.vehiculo.carroceria,
            kilometraje = "${datos.vehiculo.km.conComas()} km"
        ),
        resumenes = EstadoPieza.values().map { estado ->
            ResumenEstado(estado, estados.count { it.situacion.aEstadoPieza() == estado })
        },
        mostrarActualizarKm = mostrarActualizarKm
    )
}