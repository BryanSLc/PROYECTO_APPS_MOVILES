package com.kilo.kilo.ui.configuracion

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kilo.kilo.notificaciones.tienePermisoNotificaciones
import com.kilo.kilo.ui.common.UiStateContent
import com.kilo.kilo.ui.theme.Borde
import com.kilo.kilo.ui.theme.BordeCampo
import com.kilo.kilo.ui.theme.KiloTheme

@Composable
fun ConfiguracionScreen(viewModel: ConfiguracionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Android 13+ exige el permiso para mostrar notificaciones: se pide al encenderlas
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onPermisoNotificacionesResultado
    )

    UiStateContent(uiState) { state ->
        ConfiguracionContent(
            state = state,
            onNotificacionesChange = { activar ->
                if (activar && !contexto.tienePermisoNotificaciones()) {
                    pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onNotificacionesChange(activar)
                }
            },
            onTemaOscuroChange = viewModel::onTemaOscuroChange
        )
    }
}

@Composable
fun ConfiguracionContent(
    state: ConfiguracionState,
    onNotificacionesChange: (Boolean) -> Unit,
    onTemaOscuroChange: (Boolean) -> Unit
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
                        text = "Avisa cuando una pieza esté a punto de vencerse",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.notificacionesActivas,
                    onCheckedChange = onNotificacionesChange,
                    colors = kiloSwitchColors()
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
                        text = "Tema oscuro",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cambia entre el modo claro y el oscuro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.temaOscuro,
                    onCheckedChange = onTemaOscuroChange,
                    colors = kiloSwitchColors()
                )
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

/** Colores del switch apagado con buen contraste en tema claro y oscuro. */
@Composable
private fun kiloSwitchColors() = SwitchDefaults.colors(
    uncheckedThumbColor = BordeCampo,
    uncheckedTrackColor = Borde,
    uncheckedBorderColor = BordeCampo
)

@Preview(showBackground = true)
@Composable
private fun ConfiguracionPreview() {
    KiloTheme {
        ConfiguracionContent(
            state = ConfiguracionState(),
            onNotificacionesChange = {},
            onTemaOscuroChange = {}
        )
    }
}