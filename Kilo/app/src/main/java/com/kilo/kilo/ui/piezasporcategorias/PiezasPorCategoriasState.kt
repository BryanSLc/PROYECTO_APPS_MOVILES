package com.kilo.kilo.ui.piezasporcategorias

import androidx.compose.ui.graphics.Color
import com.kilo.kilo.ui.theme.AmbarProximo
import com.kilo.kilo.ui.theme.AmbarProximoFondo
import com.kilo.kilo.ui.theme.GrisSinDatos
import com.kilo.kilo.ui.theme.GrisSinDatosFondo
import com.kilo.kilo.ui.theme.RojoVencido
import com.kilo.kilo.ui.theme.RojoVencidoFondo
import com.kilo.kilo.ui.theme.VerdeAlDia
import com.kilo.kilo.ui.theme.VerdeAlDiaFondo


enum class EstadoPieza(
    val titulo: String,
    val descripcion: String,
    val tinta: Color,
    val fondo: Color
) {
    VENCIDOS("Vencidos", "Ya superaron su intervalo de km o de tiempo.", RojoVencido, RojoVencidoFondo),
    PROXIMOS("Próximos", "Están por cumplir su intervalo de km o de tiempo.", AmbarProximo, AmbarProximoFondo),
    AL_DIA("Al día", "Están dentro de su intervalo de km y de tiempo.", VerdeAlDia, VerdeAlDiaFondo),
    SIN_DATOS("Sin datos", "Aún no tienen un mantenimiento registrado.", GrisSinDatos, GrisSinDatosFondo)
}

data class PiezaResumen(val nombre: String, val detalle: String)


val piezasDeEjemplo: Map<EstadoPieza, List<PiezaResumen>> = mapOf(
    EstadoPieza.VENCIDOS to listOf(PiezaResumen("Filtro de aceite", "2,450 km de retraso")),
    EstadoPieza.PROXIMOS to listOf(PiezaResumen("Aceite del motor", "Faltan 1,550 km")),
    EstadoPieza.AL_DIA to listOf(
        PiezaResumen("Llantas", "Faltan 12,000 km"),
        PiezaResumen("Frenos", "Faltan 8,300 km")
    ),
    EstadoPieza.SIN_DATOS to listOf(PiezaResumen("Batería", "Sin registro"))
)

data class PiezasPorCategoriasState(
    val estadoAbierto: EstadoPieza? = null,
    val piezas: Map<EstadoPieza, List<PiezaResumen>> = piezasDeEjemplo
) {
    val piezasVisibles: List<PiezaResumen>
        get() = estadoAbierto?.let { piezas[it] }.orEmpty()

    fun cantidad(estado: EstadoPieza): Int = piezas[estado].orEmpty().size
}