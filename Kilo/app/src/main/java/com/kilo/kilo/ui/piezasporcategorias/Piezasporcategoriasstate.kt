package com.kilo.kilo.ui.piezasporcategorias

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kilo.kilo.data.Situacion
import com.kilo.kilo.ui.theme.AmbarProximo
import com.kilo.kilo.ui.theme.AmbarProximoFondo
import com.kilo.kilo.ui.theme.GrisSinDatos
import com.kilo.kilo.ui.theme.GrisSinDatosFondo
import com.kilo.kilo.ui.theme.RojoVencido
import com.kilo.kilo.ui.theme.RojoVencidoFondo
import com.kilo.kilo.ui.theme.VerdeAlDia
import com.kilo.kilo.ui.theme.VerdeAlDiaFondo

/** Los 4 estados del dashboard. */
enum class EstadoPieza(
    val titulo: String,
    val descripcion: String
) {
    VENCIDOS("Vencidos", "Ya superaron su intervalo de distancia o de tiempo."),
    PROXIMOS("Próximos", "Están por cumplir su intervalo de distancia o de tiempo."),
    AL_DIA("Al día", "Están dentro de su intervalo de distancia y de tiempo."),
    SIN_DATOS("Sin datos", "Aún no tienen un mantenimiento registrado.");

    /** Color del texto y del punto del estado; cambia con el tema claro/oscuro. */
    val tinta: Color
        @Composable get() = when (this) {
            VENCIDOS -> RojoVencido
            PROXIMOS -> AmbarProximo
            AL_DIA -> VerdeAlDia
            SIN_DATOS -> GrisSinDatos
        }

    /** Color de fondo de la etiqueta del estado; cambia con el tema claro/oscuro. */
    val fondo: Color
        @Composable get() = when (this) {
            VENCIDOS -> RojoVencidoFondo
            PROXIMOS -> AmbarProximoFondo
            AL_DIA -> VerdeAlDiaFondo
            SIN_DATOS -> GrisSinDatosFondo
        }
}

/** Une el resultado del cálculo (Situacion) con la categoría que se dibuja (EstadoPieza). */
fun Situacion.aEstadoPieza(): EstadoPieza = when (this) {
    Situacion.VENCIDO -> EstadoPieza.VENCIDOS
    Situacion.PROXIMO -> EstadoPieza.PROXIMOS
    Situacion.AL_DIA -> EstadoPieza.AL_DIA
    Situacion.SIN_DATOS -> EstadoPieza.SIN_DATOS
}

/** [clave] identifica la pieza (TipoPieza.name) para abrir su Detalle al tocarla. */
data class PiezaResumen(val nombre: String, val detalle: String, val clave: String = "")

// Datos de ejemplo, solo para el @Preview
val piezasDeEjemplo: Map<EstadoPieza, List<PiezaResumen>> = mapOf(
    EstadoPieza.VENCIDOS to listOf(PiezaResumen("Filtro de aceite", "2,450 km de retraso")),
    EstadoPieza.PROXIMOS to listOf(PiezaResumen("Aceite del motor", "Faltan 1,550 km")),
    EstadoPieza.AL_DIA to listOf(
        PiezaResumen("Llantas", "Faltan 19,550 km"),
        PiezaResumen("Frenos", "Faltan 16,550 km")
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