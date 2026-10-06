package com.kilo.kilo.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kilo.kilo.ui.theme.*

/** Datos de cada tarjeta de estado (Vencidos, Próximos, Al día, Sin datos). */
private data class ResumenEstado(
    val etiqueta: String,
    val cantidad: Int,
    val tinta: Color,
    val fondo: Color
)

/** Pestañas de la barra inferior. */
private data class Pestana(val etiqueta: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana("Inicio", Icons.Rounded.Home),
    Pestana("Vehículos", Icons.Rounded.DirectionsCar),
    Pestana("Ajustes", Icons.Rounded.Settings)
)

@Composable
fun InicioPantalla() {
    // Datos de ejemplo (más adelante vendrán del ViewModel / base de datos).
    val resumenes = listOf(
        ResumenEstado("Vencidos", 1, RojoVencido, RojoVencidoFondo),
        ResumenEstado("Próximos", 1, AmbarProximo, AmbarProximoFondo),
        ResumenEstado("Al día", 2, VerdeAlDia, VerdeAlDiaFondo),
        ResumenEstado("Sin datos", 1, GrisSinDatos, GrisSinDatosFondo)
    )

    Scaffold(
        containerColor = Fondo,
        bottomBar = { BarraInferior(seleccionada = 0) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Encabezado(nombreUsuario = "Andrea")

            TarjetaVehiculo(
                nombre = "Mazda 3 2019",
                placa = "PBX-482",
                carroceria = "Sedán",
                kilometraje = "68,450 km"
            )

            CabeceraMantenimientos()

            // Rejilla de 2 x 2
            resumenes.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { resumen ->
                        TarjetaEstado(resumen, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Encabezado

@Composable
private fun Encabezado(nombreUsuario: String) {
    Column {
        Text(
            text = "Hola, $nombreUsuario",
            style = MaterialTheme.typography.bodySmall,
            color = TintaSuave
        )
        Text(
            text = "Tu vehículo",
            style = MaterialTheme.typography.headlineSmall,
            color = Tinta
        )
    }
}

// ---------------------------------------------------------- Tarjeta vehículo

@Composable
private fun TarjetaVehiculo(
    nombre: String,
    placa: String,
    carroceria: String,
    kilometraje: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila superior: imagen, nombre y botón Cambiar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuperficieSuave)
                        .border(1.dp, Borde, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = TintaTenue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(nombre, style = MaterialTheme.typography.titleMedium, color = Tinta)
                    Text(
                        text = "$placa · $carroceria",
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }

                TextButton(onClick = { /* Sin función por ahora */ }) {
                    Text("Cambiar", color = Morado, style = MaterialTheme.typography.labelMedium)
                }
            }

            // Fila inferior: kilometraje y botón Actualizar km
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Kilometraje actual",
                        style = MaterialTheme.typography.labelSmall,
                        color = TintaSuave
                    )
                    Text(
                        text = kilometraje,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        color = Tinta
                    )
                }

                Button(
                    onClick = { /* Sin función por ahora */ },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Morado,
                        contentColor = Blanco
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text("Actualizar km", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ------------------------------------------------- Cabecera de la sección

@Composable
private fun CabeceraMantenimientos() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mantenimientos",
            style = MaterialTheme.typography.labelMedium,
            color = TintaMedia,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = { /* Sin función por ahora */ }) {
            Text("Ver historial", color = Morado, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ------------------------------------------------------- Tarjeta de estado

@Composable
private fun TarjetaEstado(resumen: ResumenEstado, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Chip con punto de color
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(resumen.fondo)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(resumen.tinta)
                )
                Spacer(Modifier.width(6.dp))
                Text(resumen.etiqueta, style = MaterialTheme.typography.labelSmall, color = resumen.tinta)
            }

            Spacer(Modifier.height(16.dp))

            // Cantidad + "pieza(s)" + flecha
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = resumen.cantidad.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tinta
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = if (resumen.cantidad == 1) "pieza" else "piezas",
                    style = MaterialTheme.typography.bodySmall,
                    color = TintaSuave,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(resumen.fondo),
                    contentAlignment = Alignment.Center
                ) {
                    Text("›", color = resumen.tinta, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --------------------------------------------------------- Barra inferior

@Composable
private fun BarraInferior(seleccionada: Int) {
    NavigationBar(containerColor = Superficie) {
        PESTANAS.forEachIndexed { indice, pestana ->
            NavigationBarItem(
                selected = indice == seleccionada,
                onClick = { /* Sin navegación por ahora */ },
                icon = { Icon(pestana.icono, contentDescription = pestana.etiqueta) },
                label = { Text(pestana.etiqueta, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Morado,
                    selectedTextColor = Morado,
                    unselectedIconColor = TintaTenue,
                    unselectedTextColor = TintaTenue,
                    indicatorColor = MoradoSuave
                )
            )
        }
    }
}

// ------------------------------------------------------------------ Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioPantallaPreview() {
    KiloTheme {
        InicioPantalla()
    }
}