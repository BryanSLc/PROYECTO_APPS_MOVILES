package com.kilo.kilo.ui.piezasporcategorias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kilo.kilo.ui.common.datosOrNull
import com.kilo.kilo.ui.theme.*

/**
 * Hoja flotante que se abre al tocar cualquiera de las 4 tarjetas del dashboard.
 * Es una vista sin estado: recibe el estado y las lambdas; el ViewModel vive en InicioPantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiezasPorCategorias(
    state: PiezasPorCategoriasState,
    onCerrar: () -> Unit,
    onPiezaClick: (PiezaResumen) -> Unit
) {
    // La hoja solo se dibuja cuando hay una categoría abierta
    val estado = state.estadoAbierto ?: return

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Superficie,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        PiezasPorCategoriasContent(
            estado = estado,
            piezas = state.piezasVisibles,
            onCerrar = onCerrar,
            onPiezaClick = onPiezaClick
        )
    }
}

@Composable
fun PiezasPorCategoriasContent(
    estado: EstadoPieza,
    piezas: List<PiezaResumen>,
    onCerrar: () -> Unit,
    onPiezaClick: (PiezaResumen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(estado.tinta))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${estado.titulo} (${piezas.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Tinta
                )
            }
            TextButton(onClick = onCerrar) {
                Text("Cerrar", color = Morado, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Text(estado.descripcion, style = MaterialTheme.typography.bodySmall, color = TintaSuave)

        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            piezas.forEach { pieza ->
                FilaPieza(pieza, estado.tinta) { onPiezaClick(pieza) }
            }
        }
    }
}

@Composable
private fun FilaPieza(pieza: PiezaResumen, tinta: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Borde, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(tinta))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                pieza.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Tinta
            )
            Text(pieza.detalle, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = TintaTenue)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF8A8A97)
@Composable
private fun PiezasVencidosPreview() {
    KiloTheme {
        Box(Modifier.background(Superficie, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
            PiezasPorCategoriasContent(
                estado = EstadoPieza.VENCIDOS,
                piezas = piezasDeEjemplo.getValue(EstadoPieza.VENCIDOS),
                onCerrar = {},
                onPiezaClick = {}
            )
        }
    }
}