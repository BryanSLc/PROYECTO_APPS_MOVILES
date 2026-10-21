package com.kilo.kilo.ui.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Estado de una pantalla que carga datos: cargando, con datos, o con error.
 * Cada ViewModel expone UN solo StateFlow<UiState<...>>.
 */
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
}

/** Envuelve cada valor del Flow en Success y convierte una excepción en Error. */
fun <T> Flow<T>.comoUiState(): Flow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Ocurrió un error inesperado")) }

/** Los datos si ya cargaron; null mientras carga o si hubo error. */
fun <T> UiState<T>.datosOrNull(): T? = (this as? UiState.Success)?.data
