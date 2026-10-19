package com.kilo.kilo.ui.principal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.Preferencias
import com.kilo.kilo.data.PreferenciasRepository
import com.kilo.kilo.notificaciones.NotificadorMantenimiento
import com.kilo.kilo.ui.common.UiState
import com.kilo.kilo.ui.common.comoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PrincipalViewModel @Inject constructor(
    private val preferencias: PreferenciasRepository,
    private val repositorio: KiloRepository,
    private val notificador: NotificadorMantenimiento
) : ViewModel() {

    val uiState: StateFlow<UiState<PrincipalState>> = preferencias.preferencias
        .map { it.aEstado() }
        .comoUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // Ya con el tema guardado, para que no parpadee el tema claro al abrir en oscuro
            initialValue = UiState.Success(preferencias.preferencias.value.aEstado())
        )

    init {
        // Cada vez que cambian los datos (km nuevo, mantenimiento registrado...) o el ajuste de
        // notificaciones, se revisa si alguna pieza pasó a "Próximo" para avisar en el teléfono.
        viewModelScope.launch {
            combine(
                repositorio.datos,
                preferencias.preferencias.map { it.notificacionesActivas }.distinctUntilChanged()
            ) { datos, activas -> datos to activas }
                .catch { /* si falla la lectura, simplemente no se avisa */ }
                .collect { (datos, activas) -> if (activas) notificador.revisar(datos) }
        }
    }

    fun onPermisoNotificacionesSolicitado() {
        preferencias.marcarPermisoNotificacionesSolicitado()
    }

    /** Resultado del permiso: si lo negó, el ajuste queda apagado; si lo concedió, se revisa de inmediato. */
    fun onPermisoNotificacionesResultado(concedido: Boolean) {
        if (!concedido) {
            preferencias.setNotificaciones(false)
            return
        }
        viewModelScope.launch { notificador.revisar(repositorio.datos.first()) }
    }
}

private fun Preferencias.aEstado() = PrincipalState(
    temaOscuro = temaOscuro,
    debePedirPermisoNotificaciones = notificacionesActivas && !permisoNotificacionesSolicitado
)