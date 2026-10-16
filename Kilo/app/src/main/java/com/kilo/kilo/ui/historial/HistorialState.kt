package com.kilo.kilo.ui.historial

const val FILTRO_TODOS = "Todos"

data class RegistroHistorial(
    val id: Long,
    val tipo: String,
    val abreviatura: String,
    val titulo: String,
    val fecha: String,
    val kilometraje: String,
    val nota: String
)

data class HistorialState(
    val cargando: Boolean = false,
    val vehiculo: String = "",
    val filtros: List<String> = listOf(FILTRO_TODOS),
    val filtroSeleccionado: String = FILTRO_TODOS,
    val registros: List<RegistroHistorial> = emptyList(),
    val error: String? = null
) {
    val registrosVisibles: List<RegistroHistorial>
        get() = if (filtroSeleccionado == FILTRO_TODOS) {
            registros
        } else {
            registros.filter { it.tipo == filtroSeleccionado }
        }
}