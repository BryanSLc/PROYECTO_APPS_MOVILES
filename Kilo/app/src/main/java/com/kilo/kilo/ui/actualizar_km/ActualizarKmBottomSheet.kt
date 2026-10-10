package com.kilo.kilo.ui.actualizar_km

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kilo.kilo.ui.theme.KiloTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarKmBottomSheet(
    onDismiss: () -> Unit,
    viewModel: ActualizarKmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onCancelar()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        ActualizarKmContent(
            state = state,
            onKmChange = viewModel::onKmChange,
            onCancelar = {
                viewModel.onCancelar()
                onDismiss()
            },
            onConfirmar = {
                viewModel.onConfirmar()
                onDismiss()
            }
        )
    }
}

@Composable
fun ActualizarKmContent(
    state: ActualizarKmState,
    onKmChange: (String) -> Unit,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Actualizar kilometraje",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${state.vehiculo} · antes ${"%,d".format(state.kmActual)} km",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.kmNuevo,
            onValueChange = onKmChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 28.sp
                )
            },
            suffix = { Text("km") },
            textStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirmar,
                enabled = state.puedeConfirmar,
                modifier = Modifier.weight(2f)
            ) {
                Text("Confirmar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActualizarKmPreview() {
    KiloTheme {
        ActualizarKmContent(
            state = ActualizarKmState(
                vehiculo = "Mazda 3 2019",
                kmActual = 68450,
                kmNuevo = ""
            ),
            onKmChange = {},
            onCancelar = {},
            onConfirmar = {}
        )
    }
}