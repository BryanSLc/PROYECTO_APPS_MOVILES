package com.kilo.kilo.ui.historial

data class HistorialState(
    val isLoading: Boolean = false,
    val historialList: List<String> = emptyList(),
    val errorMessage: String? = null
)