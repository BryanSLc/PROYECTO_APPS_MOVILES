package com.kilo.kilo.ui.registrarmantenimiento

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberUpdatedState
import com.kilo.kilo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarMantenimiento(
    onCancelar: () -> Unit,
    onGuardar: () -> Unit,
    tipoInicial: String? = null,
    viewModel: RegistrarMantenimientoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Al abrirse, deja elegida la pieza, la fecha de hoy y el km actual
    LaunchedEffect(Unit) { viewModel.preparar(tipoInicial) }

    // Evento de una sola vez: cuando se guardó bien, se cierra la hoja
    val alGuardar by rememberUpdatedState(onGuardar)
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { alGuardar() }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onCancelar()
            onCancelar()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Superficie,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        RegistrarMantenimientoContent(
            state = state,
            onTipoChange = viewModel::onTipoChange,
            onFechaChange = viewModel::onFechaChange,
            onKmChange = viewModel::onKmChange,
            onNotasChange = viewModel::onNotasChange,
            onCancelar = {
                viewModel.onCancelar()
                onCancelar()
            },
            onGuardar = viewModel::onGuardar
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrarMantenimientoContent(
    state: RegistrarMantenimientoState,
    onTipoChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onKmChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Text(
            "Registrar mantenimiento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Tinta
        )

        Spacer(Modifier.height(16.dp))
        Etiqueta("Tipo")
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.tipos.forEach { opcion ->
                ChipTipo(opcion, opcion == state.tipoSeleccionado) { onTipoChange(opcion) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Etiqueta("Fecha")
                Spacer(Modifier.height(6.dp))
                Campo(
                    valor = state.fecha,
                    onCambio = onFechaChange,
                    trailing = { Icon(Icons.Filled.DateRange, contentDescription = null, tint = TintaSuave) }
                )
            }
            Column(Modifier.weight(1f)) {
                Etiqueta("Km del cambio")
                Spacer(Modifier.height(6.dp))
                Campo(valor = state.kmDelCambio, onCambio = onKmChange)
            }
        }

        Spacer(Modifier.height(16.dp))
        Etiqueta("Notas (opcional)")
        Spacer(Modifier.height(6.dp))
        Campo(
            valor = state.notas,
            onCambio = onNotasChange,
            placeholder = "Taller, refacción usada...",
            alto = 80.dp,
            unaLinea = false
        )

        state.error?.let { mensaje ->
            Spacer(Modifier.height(10.dp))
            Text(mensaje, fontSize = 12.sp, color = RojoVencido)
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Borde),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Tinta)
            ) { Text("Cancelar", fontWeight = FontWeight.SemiBold) }
            Button(
                onClick = onGuardar,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Morado, contentColor = Blanco)
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TintaMedia)
}

@Composable
private fun ChipTipo(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (seleccionado) MoradoSuave else Superficie)
            .border(1.dp, if (seleccionado) Morado else Borde, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (seleccionado) Morado else TintaMedia
        )
    }
}

@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String = "",
    alto: Dp = 48.dp,
    unaLinea: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth().height(alto),
        singleLine = unaLinea,
        shape = RoundedCornerShape(10.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = { Text(placeholder, color = TintaTenue, fontSize = 13.sp) },
        trailingIcon = trailing,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Morado,
            unfocusedBorderColor = Borde,
            focusedTextColor = Tinta,
            unfocusedTextColor = Tinta,
            focusedContainerColor = Superficie,
            unfocusedContainerColor = Superficie
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF8A8A97)
@Composable
private fun RegistrarPreview() {
    KiloTheme {
        Box(Modifier.background(Superficie, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
            RegistrarMantenimientoContent(
                state = RegistrarMantenimientoState(),
                onTipoChange = {}, onFechaChange = {}, onKmChange = {}, onNotasChange = {},
                onCancelar = {}, onGuardar = {}
            )
        }
    }
}