package com.kilo.kilo.ui.mis_vehiculos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kilo.kilo.ui.common.UiStateContent
import com.kilo.kilo.ui.theme.AmbarProximo
import com.kilo.kilo.ui.theme.GrisSinDatos
import com.kilo.kilo.ui.theme.KiloTheme
import com.kilo.kilo.ui.theme.RojoVencido
import com.kilo.kilo.ui.theme.VerdeAlDia

@Composable
private fun EstadoMantenimiento.color(): Color = when (this) {
    EstadoMantenimiento.VENCIDO -> RojoVencido
    EstadoMantenimiento.PROXIMO -> AmbarProximo
    EstadoMantenimiento.AL_DIA -> VerdeAlDia
    EstadoMantenimiento.SIN_DATOS -> GrisSinDatos
}

@Composable
fun MisVehiculosScreen(
    onAgregar: () -> Unit = {},
    viewModel: MisVehiculosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UiStateContent(uiState) { state ->
        MisVehiculosContent(
            state = state,
            onAgregar = onAgregar,
            onSeleccionar = viewModel::onSeleccionar,
            onEliminar = viewModel::onEliminar
        )
    }
}

@Composable
fun MisVehiculosContent(
    state: MisVehiculosState,
    onAgregar: () -> Unit,
    onSeleccionar: (Long) -> Unit,
    onEliminar: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis vehículos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            FilledIconButton(onClick = onAgregar) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar vehículo"
                )
            }
        }

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.vehiculos, key = { it.id }) { vehiculo ->
                VehiculoItem(
                    vehiculo = vehiculo,
                    onSeleccionar = { onSeleccionar(vehiculo.id) },
                    onEliminar = { onEliminar(vehiculo.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VehiculoItem(
    vehiculo: VehiculoUi,
    onSeleccionar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onSeleccionar)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vehiculo.tipo.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = vehiculo.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${vehiculo.placa} · ${vehiculo.tipo} · ${vehiculo.kilometraje}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (vehiculo.activo) {
                        Text(
                            text = "Vehículo activo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (vehiculo.vencidos > 0) {
                    Text(
                        text = "${vehiculo.vencidos} vencido(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = RojoVencido,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                vehiculo.mantenimientos.forEach { mantenimiento ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(mantenimiento.estado.color(), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mantenimiento.nombre,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEliminar) {
                    Text(
                        text = "Eliminar",
                        color = RojoVencido,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MisVehiculosPreview() {
    KiloTheme {
        MisVehiculosContent(
            state = MisVehiculosState(
                vehiculos = listOf(
                    VehiculoUi(
                        id = 1,
                        nombre = "Mazda 3 2019",
                        placa = "PBX-482",
                        tipo = "Sedán",
                        kilometraje = "68,450 km",
                        mantenimientos = listOf(
                            MantenimientoUi("Aceite", EstadoMantenimiento.VENCIDO),
                            MantenimientoUi("Filtro", EstadoMantenimiento.VENCIDO),
                            MantenimientoUi("Llantas", EstadoMantenimiento.AL_DIA),
                            MantenimientoUi("Frenos", EstadoMantenimiento.AL_DIA),
                            MantenimientoUi("Batería", EstadoMantenimiento.SIN_DATOS)
                        )
                    ),
                    VehiculoUi(
                        id = 2,
                        nombre = "Toyota Hilux 2021",
                        placa = "TQR-115",
                        tipo = "Pickup",
                        kilometraje = "41,200 km",
                        mantenimientos = listOf(
                            MantenimientoUi("Aceite", EstadoMantenimiento.AL_DIA),
                            MantenimientoUi("Filtro", EstadoMantenimiento.SIN_DATOS),
                            MantenimientoUi("Llantas", EstadoMantenimiento.AL_DIA),
                            MantenimientoUi("Frenos", EstadoMantenimiento.VENCIDO),
                            MantenimientoUi("Batería", EstadoMantenimiento.SIN_DATOS)
                        )
                    )
                )
            ),
            onAgregar = {},
            onSeleccionar = {},
            onEliminar = {}
        )
    }
}