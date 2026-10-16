package com.kilo.kilo.ui.historial

data class RegistroHistorial(
    val id: Int,
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
    val filtros: List<String> = listOf("Todos", "Aceite", "Filtro", "Llantas", "Frenos", "Batería"),
    val filtroSeleccionado: String = "Todos",
    val registros: List<RegistroHistorial> = emptyList(),
    val error: String? = null
) {
    val registrosVisibles: List<RegistroHistorial>
        get() = if (filtroSeleccionado == "Todos") {
            registros
        } else {
            registros.filter { it.tipo == filtroSeleccionado }
        }
}