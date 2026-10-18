package com.kilo.kilo.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kilo.kilo.ui.theme.Blanco
import com.kilo.kilo.ui.theme.KiloTheme
import com.kilo.kilo.ui.theme.Morado

@Composable
fun SplashScreen(
    onTerminar: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.terminado) {
        if (state.terminado) onTerminar()
    }
    SplashContent(state)
}

@Composable
fun SplashContent(state: SplashState) {
    val progreso by animateFloatAsState(targetValue = state.progreso, label = "progreso")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Morado),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Blanco),
                contentAlignment = Alignment.Center
            ) {
                Text("K", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Morado)
            }
            Spacer(Modifier.height(16.dp))
            Text("Kilo", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Blanco)
            Spacer(Modifier.height(6.dp))
            Text(
                "Mantenimiento al día, sin sorpresas",
                fontSize = 13.sp,
                color = Blanco.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Blanco.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(64.dp * progreso)
                        .clip(CircleShape)
                        .background(Blanco)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
private fun SplashPreview() {
    KiloTheme { SplashContent(SplashState(progreso = 0.35f)) }
}