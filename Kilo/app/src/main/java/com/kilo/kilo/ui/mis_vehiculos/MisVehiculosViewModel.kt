package com.kilo.kilo.ui.mis_vehiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.CalculoMantenimiento
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.Situacion
import com.kilo.kilo.data.conComas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MisVehiculosViewModel @Inject constructor(
    private val repositorio: KiloRepository
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    val state: StateFlow<MisVehiculosState> = combine(
        repositorio.datos,
        error
    ) { datos, mensaje -> construirEstado(datos, mensaje) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = construirEstado(repositorio.datos.value, null)
        )

    /** Toca un vehículo: pasa a ser el que se muestra en Inicio. */
    fun onSeleccionar(id: Long) {
        error.value = null
        repositorio.seleccionarVehiculo(id)
    }

    fun onEliminar(id: Long) {
        error.value = repositorio.eliminarVehiculo(id)
    }
}

private fun construirEstado(datos: DatosKilo, error: String?): MisVehiculosState =
    MisVehiculosState(
        vehiculos = datos.vehiculos.map { v ->
            VehiculoUi(
                id = v.id,
                nombre = v.nombre,
                placa = v.placa,
                tipo = v.carroceria,
                kilometraje = "${v.km.conComas()} km",
                mantenimientos = CalculoMantenimiento.estadosDe(v).map { e ->
                    MantenimientoUi(
                        nombre = e.pieza.nombre,
                        estado = when (e.situacion) {
                            Situacion.VENCIDO -> EstadoMantenimiento.VENCIDO
                            Situacion.PROXIMO -> EstadoMantenimiento.PROXIMO
                            Situacion.AL_DIA -> EstadoMantenimiento.AL_DIA
                            Situacion.SIN_DATOS -> EstadoMantenimiento.SIN_DATOS
                        }
                    )
                },
                activo = v.id == datos.vehiculoActivoId
            )
        },
        error = error
    )
