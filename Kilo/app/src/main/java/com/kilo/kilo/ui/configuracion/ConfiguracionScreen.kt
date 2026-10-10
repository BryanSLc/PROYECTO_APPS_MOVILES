package com.kilo.kilo.ui.configuracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kilo.kilo.ui.theme.KiloTheme

@Composable
fun ConfiguracionScreen(viewModel: ConfiguracionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConfiguracionContent(
        state = state,
        onNotificacionesChange = viewModel::onNotificacionesChange,
        onUnidadChange = viewModel::onUnidadChange
    )
}

@Composable
fun ConfiguracionContent(
    state: ConfiguracionState,
    onNotificacionesChange: (Boolean) -> Unit,
    onUnidadChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Se guarda en este dispositivo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Avisos de mantenimientos próximos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.notificacionesActivas,
                    onCheckedChange = onNotificacionesChange
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unidad de distancia",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Afecta todas las pantallas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.unidadDistancia == "km",
                        onClick = { onUnidadChange("km") },
                        label = { Text("km") }
                    )
                    FilterChip(
                        selected = state.unidadDistancia == "mi",
                        onClick = { onUnidadChange("mi") },
                        label = { Text("mi") }
                    )
                }
            }
        }

        Text(
            text = "Preferencias en almacenamiento local ligero; los vehículos y mantenimientos viven en la base de datos local.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracionPreview() {
    KiloTheme {
        ConfiguracionContent(
            state = ConfiguracionState(),
            onNotificacionesChange = {},
            onUnidadChange = {}
        )
    }
}