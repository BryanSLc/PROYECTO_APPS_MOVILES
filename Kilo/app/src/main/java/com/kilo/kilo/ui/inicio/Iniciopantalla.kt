package com.kilo.kilo.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kilo.kilo.ui.theme.*


private data class Pestana(val etiqueta: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana("Inicio", Icons.Rounded.Home),
    Pestana("Vehículos", Icons.Rounded.DirectionsCar),
    Pestana("Ajustes", Icons.Rounded.Settings)
)


@Composable
fun InicioPantalla(
    onVerHistorial: () -> Unit = {},
    onCambiarVehiculo: () -> Unit = {},
    onIrAVehiculos: () -> Unit = {},
    onIrAAjustes: () -> Unit = {},
    onPiezaClick: (PiezaResumen) -> Unit = {},
    viewModel: InicioViewModel = hiltViewModel(),
    piezasViewModel: PiezasPorCategoriasViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    InicioContent(
        state = state,
        onCambiarVehiculo = onCambiarVehiculo,
        onActualizarKm = viewModel::onActualizarKmClick,
        onVerHistorial = onVerHistorial,
        onTarjetaClick = piezasViewModel::onTarjetaClick,
        onPestanaClick = { indice ->
            when (indice) {
                1 -> onIrAVehiculos()
                2 -> onIrAAjustes()
            }
        }
    )

    PiezasPorCategorias(onPiezaClick = onPiezaClick)

}

@Composable
fun InicioContent(
    state: InicioState,
    onCambiarVehiculo: () -> Unit,
    onActualizarKm: () -> Unit,
    onVerHistorial: () -> Unit,
    onTarjetaClick: (EstadoPieza) -> Unit,
    onPestanaClick: (Int) -> Unit
) {
    Scaffold(
        containerColor = Fondo,
        bottomBar = { BarraInferior(seleccionada = 0, onPestanaClick = onPestanaClick) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Encabezado(nombreUsuario = state.nombreUsuario)

            TarjetaVehiculo(
                vehiculo = state.vehiculo,
                onCambiar = onCambiarVehiculo,
                onActualizarKm = onActualizarKm
            )

            CabeceraMantenimientos(onVerHistorial)

            // Rejilla de 2 x 2
            state.resumenes.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { resumen ->
                        TarjetaEstado(
                            resumen = resumen,
                            onClick = { onTarjetaClick(resumen.estado) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}



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



@Composable
private fun TarjetaVehiculo(
    vehiculo: VehiculoResumen,
    onCambiar: () -> Unit,
    onActualizarKm: () -> Unit
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
                    Text(vehiculo.nombre, style = MaterialTheme.typography.titleMedium, color = Tinta)
                    Text(
                        text = "${vehiculo.placa} · ${vehiculo.carroceria}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }

                TextButton(onClick = onCambiar) {
                    Text("Cambiar", color = Morado, style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Kilometraje actual",
                        style = MaterialTheme.typography.labelSmall,
                        color = TintaSuave
                    )
                    Text(
                        text = vehiculo.kilometraje,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        color = Tinta
                    )
                }

                Button(
                    onClick = onActualizarKm,
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



@Composable
private fun CabeceraMantenimientos(onVerHistorial: () -> Unit) {
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
        TextButton(onClick = onVerHistorial) {
            Text("Ver historial", color = Morado, style = MaterialTheme.typography.labelMedium)
        }
    }
}


@Composable
private fun TarjetaEstado(
    resumen: ResumenEstado,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tinta = resumen.estado.tinta
    val fondo = resumen.estado.fondo

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(fondo)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(tinta)
                )
                Spacer(Modifier.width(6.dp))
                Text(resumen.estado.titulo, style = MaterialTheme.typography.labelSmall, color = tinta)
            }

            Spacer(Modifier.height(16.dp))

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
                        .background(fondo),
                    contentAlignment = Alignment.Center
                ) {
                    Text("›", color = tinta, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



@Composable
private fun BarraInferior(seleccionada: Int, onPestanaClick: (Int) -> Unit) {
    NavigationBar(containerColor = Superficie) {
        PESTANAS.forEachIndexed { indice, pestana ->
            NavigationBarItem(
                selected = indice == seleccionada,
                onClick = { onPestanaClick(indice) },
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



@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioPantallaPreview() {
    KiloTheme {
        InicioContent(
            state = InicioState(),
            onCambiarVehiculo = {},
            onActualizarKm = {},
            onVerHistorial = {},
            onTarjetaClick = {},
            onPestanaClick = {}
        )
    }
}s