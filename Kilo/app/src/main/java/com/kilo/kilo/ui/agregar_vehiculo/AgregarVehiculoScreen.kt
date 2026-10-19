package com.kilo.kilo.ui.agregar_vehiculo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import com.kilo.kilo.ui.theme.Borde
import com.kilo.kilo.ui.theme.BordeCampo
import com.kilo.kilo.ui.theme.KiloTheme
import com.kilo.kilo.ui.theme.Superficie
import com.kilo.kilo.ui.theme.SuperficieSuave
import com.kilo.kilo.ui.theme.TintaMedia

@Composable
fun AgregarVehiculoScreen(
    onVolver: () -> Unit = {},
    viewModel: AgregarVehiculoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Evento de una sola vez: cuando se guardó bien, vuelve a "Mis vehículos"
    val alVolver by rememberUpdatedState(onVolver)
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { alVolver() }
    }

    AgregarVehiculoContent(
        state = state,
        onVolver = onVolver,
        onMarcaChange = viewModel::onMarcaChange,
        onModeloChange = viewModel::onModeloChange,
        onAnioChange = viewModel::onAnioChange,
        onPlacaChange = viewModel::onPlacaChange,
        onKilometrajeChange = viewModel::onKilometrajeChange,
        onTipoChange = viewModel::onTipoChange,
        onPiezaNombreChange = viewModel::onPiezaNombreChange,
        onPiezaLimiteChange = viewModel::onPiezaLimiteChange,
        onPiezaMesesChange = viewModel::onPiezaMesesChange,
        onPiezaUltimoKmChange = viewModel::onPiezaUltimoKmChange,
        onAgregarPieza = viewModel::onAgregarPieza,
        onQuitarPieza = viewModel::onQuitarPieza,
        onGuardar = viewModel::onGuardar
    )
}

private val FormaCampo = RoundedCornerShape(12.dp)

/** Campos con fondo blanco y borde gris bien marcado; al enfocarse, borde morado más grueso. */
@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Superficie,
    unfocusedContainerColor = Superficie,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = BordeCampo,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = TintaMedia,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun TarjetaSeccion(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        border = BorderStroke(1.dp, Borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgregarVehiculoContent(
    state: AgregarVehiculoState,
    onVolver: () -> Unit,
    onMarcaChange: (String) -> Unit,
    onModeloChange: (String) -> Unit,
    onAnioChange: (String) -> Unit,
    onPlacaChange: (String) -> Unit,
    onKilometrajeChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onPiezaNombreChange: (Long, String) -> Unit,
    onPiezaLimiteChange: (Long, String) -> Unit,
    onPiezaMesesChange: (Long, String) -> Unit,
    onPiezaUltimoKmChange: (Long, String) -> Unit,
    onAgregarPieza: () -> Unit,
    onQuitarPieza: (Long) -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TextButton(
            onClick = onVolver,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("‹ Mis vehículos")
        }

        Text(
            text = "Agregar vehículo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TarjetaSeccion {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Campo("Marca", state.marca, onMarcaChange, Modifier.weight(1f))
                    Campo("Modelo", state.modelo, onModeloChange, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Campo("Año", state.anio, onAnioChange, Modifier.weight(1f), numerico = true)
                    Campo("Placa", state.placa, onPlacaChange, Modifier.weight(1f))
                }
                Campo(
                    etiqueta = "Kilometraje actual (km)",
                    valor = state.kilometraje,
                    onCambio = onKilometrajeChange,
                    modifier = Modifier.fillMaxWidth(),
                    numerico = true
                )

                Text(
                    text = "Tipo de vehículo",
                    style = MaterialTheme.typography.labelLarge,
                    color = TintaMedia
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.tipos.forEach { tipo ->
                        FilterChip(
                            selected = tipo == state.tipoSeleccionado,
                            onClick = { onTipoChange(tipo) },
                            label = { Text(tipo, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }
        }

        TarjetaSeccion(modifier = Modifier.padding(top = 12.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Piezas y límites de cambio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Define cada cuántos km (y, si quieres, cada cuántos meses) se cambia cada pieza. " +
                        "El estado se calcula con el kilometraje recorrido. Si dejas vacío el último " +
                        "cambio, la pieza aparecerá como \"sin datos\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                state.piezas.forEach { pieza ->
                    PiezaEditor(
                        pieza = pieza,
                        onNombreChange = { onPiezaNombreChange(pieza.id, it) },
                        onLimiteChange = { onPiezaLimiteChange(pieza.id, it) },
                        onMesesChange = { onPiezaMesesChange(pieza.id, it) },
                        onUltimoKmChange = { onPiezaUltimoKmChange(pieza.id, it) },
                        onQuitar = { onQuitarPieza(pieza.id) }
                    )
                }

                OutlinedButton(
                    onClick = onAgregarPieza,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("+ Agregar pieza")
                }
            }
        }

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onGuardar,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(vertical = 0.dp)
        ) {
            Text("Guardar vehículo")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PiezaEditor(
    pieza: PiezaForm,
    onNombreChange: (String) -> Unit,
    onLimiteChange: (String) -> Unit,
    onMesesChange: (String) -> Unit,
    onUltimoKmChange: (String) -> Unit,
    onQuitar: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SuperficieSuave,
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = pieza.nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Pieza") },
                    singleLine = true,
                    shape = FormaCampo,
                    colors = coloresCampo(),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onQuitar) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar pieza",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pieza.limiteKm,
                    onValueChange = onLimiteChange,
                    label = { Text("Cambio cada") },
                    singleLine = true,
                    suffix = { Text("km") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = FormaCampo,
                    colors = coloresCampo(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pieza.meses,
                    onValueChange = onMesesChange,
                    label = { Text("Meses (opc.)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = FormaCampo,
                    colors = coloresCampo(),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = pieza.ultimoKm,
                onValueChange = onUltimoKmChange,
                label = { Text("Último cambio (opcional)") },
                singleLine = true,
                suffix = { Text("km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = FormaCampo,
                colors = coloresCampo(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    numerico: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text
        ),
        shape = FormaCampo,
        colors = coloresCampo(),
        modifier = modifier
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AgregarVehiculoPreview() {
    KiloTheme {
        AgregarVehiculoContent(
            state = AgregarVehiculoState(
                marca = "Mazda",
                modelo = "Mazda 3",
                anio = "2019",
                placa = "PBX-482",
                kilometraje = "68450"
            ),
            onVolver = {},
            onMarcaChange = {},
            onModeloChange = {},
            onAnioChange = {},
            onPlacaChange = {},
            onKilometrajeChange = {},
            onTipoChange = {},
            onPiezaNombreChange = { _, _ -> },
            onPiezaLimiteChange = { _, _ -> },
            onPiezaMesesChange = { _, _ -> },
            onPiezaUltimoKmChange = { _, _ -> },
            onAgregarPieza = {},
            onQuitarPieza = {},
            onGuardar = {}
        )
    }
}