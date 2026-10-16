package com.kilo.kilo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = Morado,
    onPrimary = Blanco,
    primaryContainer = MoradoSuave,
    onPrimaryContainer = MoradoOscuro,
    background = Fondo,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = SuperficieSuave,
    onSurfaceVariant = TintaSuave,
    error = RojoVencido,
    outline = Borde
)

@Composable
fun KiloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = TipografiaKilo,
        content = content
    )
}