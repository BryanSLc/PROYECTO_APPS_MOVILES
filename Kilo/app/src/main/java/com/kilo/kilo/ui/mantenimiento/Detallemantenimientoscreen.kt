package com.kilo.kilo.ui.mantenimiento

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun DetalleMantenimientoScreen(
    onVolver: () -> Unit = {},
    viewModel: DetalleMantenimientoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetalleMantenimientoContent(
        state = state,
        onVolver = onVolver,
        onMarcarRealizado = viewModel::onMarcarRealizado
    )

    if (state.mostrarRegistro) {
        RegistrarMantenimiento(
            onCancelar = viewModel::onCerrarRegistro,
            onGuardar = viewModel::onCerrarRegistro
        )
    }
}

@Composable
fun DetalleMantenimientoContent(
    state: DetalleMantenimientoState,
    onVolver: () -> Unit,
    onMarcarRealizado: () -> Unit
) {
    Scaffold(
        containerColor = Fondo,
        bottomBar = { BarraInferior() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                "‹ Inicio",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Morado,
                modifier = Modifier.clickable(onClick = onVolver)
            )

            Spacer(Modifier.height(14.dp))
            Text(state.titulo, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Tinta)
            Text(state.intervalo, fontSize = 12.sp, color = TintaSuave)

            Spacer(Modifier.height(16.dp))
            TarjetaEstado(state)

            Spacer(Modifier.height(14.dp))
            TarjetaHistorial(state.historial)

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onMarcarRealizado,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Morado, contentColor = Blanco)
            ) {
                Text("Marcar como realizado", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun TarjetaEstado(state: DetalleMantenimientoState) {
    val tinta = state.estado.tinta
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Superficie)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(tinta))
            Spacer(Modifier.width(8.dp))
            Text(state.etiquetaEstado, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tinta)
        }

        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Borde)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progreso.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(tinta)
            )
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Último: ${state.ultimoKm}", fontSize = 11.sp, color = TintaSuave)
            Text("Meta: ${state.metaKm}", fontSize = 11.sp, color = TintaSuave)
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DatoCaja("Próximo cambio", state.proximoCambio, Modifier.weight(1f))
            DatoCaja("O antes de", state.fechaLimite, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DatoCaja(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SuperficieSuave)
            .padding(12.dp)
    ) {
        Text(titulo, fontSize = 11.sp, color = TintaSuave)
        Text(valor, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Tinta)
    }
}

@Composable
private fun TarjetaHistorial(historial: List<RegistroDetalle>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Superficie)
            .padding(16.dp)
    ) {
        Text("Historial", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Tinta)
        Spacer(Modifier.height(12.dp))
        historial.forEachIndexed { i, registro ->
            FilaHistorial(registro, esUltima = i == historial.lastIndex)
        }
    }
}

@Composable
private fun FilaHistorial(registro: RegistroDetalle, esUltima: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Morado)
            )
            if (!esUltima) {
                Box(Modifier.weight(1f).width(1.dp).background(Borde))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (esUltima) 0.dp else 16.dp)) {
            Text(registro.km, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Tinta)
            Text(registro.detalle, fontSize = 11.sp, color = TintaSuave)
        }
    }
}


@Composable
private fun BarraInferior() {
    NavigationBar(containerColor = Superficie) {
        ItemBarra("Inicio", Icons.Filled.Home, seleccionado = true)
        ItemBarra("Vehículos", Icons.Filled.DirectionsCar, seleccionado = false)
        ItemBarra("Ajustes", Icons.Filled.Settings, seleccionado = false)
    }
}

@Composable
private fun RowScope.ItemBarra(texto: String, icono: ImageVector, seleccionado: Boolean) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = { },
        icon = { Icon(icono, contentDescription = texto) },
        label = { Text(texto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Morado,
            selectedTextColor = Morado,
            indicatorColor = MoradoSuave,
            unselectedIconColor = TintaTenue,
            unselectedTextColor = TintaTenue
        )
    )
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
private fun DetalleMantenimientoPreview() {
    KiloTheme {
        DetalleMantenimientoContent(
            state = DetalleMantenimientoState(),
            onVolver = {},
            onMarcarRealizado = {}
        )
    }
}