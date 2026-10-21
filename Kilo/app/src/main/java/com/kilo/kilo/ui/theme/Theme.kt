package com.kilo.kilo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val EsquemaClaro = lightColorScheme(
    primary = PaletaClara.Morado,
    onPrimary = PaletaClara.SobreMorado,
    primaryContainer = PaletaClara.MoradoSuave,
    onPrimaryContainer = PaletaClara.MoradoOscuro,
    background = PaletaClara.Fondo,
    onBackground = PaletaClara.Tinta,
    surface = PaletaClara.Superficie,
    onSurface = PaletaClara.Tinta,
    surfaceVariant = PaletaClara.SuperficieSuave,
    onSurfaceVariant = PaletaClara.TintaSuave,
    error = PaletaClara.RojoVencido,
    outline = PaletaClara.Borde
)

private val EsquemaOscuro = darkColorScheme(
    primary = PaletaOscura.Morado,
    onPrimary = PaletaOscura.SobreMorado,
    primaryContainer = PaletaOscura.MoradoSuave,
    onPrimaryContainer = PaletaOscura.MoradoOscuro,
    secondaryContainer = PaletaOscura.MoradoSuave,
    onSecondaryContainer = PaletaOscura.Tinta,
    background = PaletaOscura.Fondo,
    onBackground = PaletaOscura.Tinta,
    surface = PaletaOscura.Superficie,
    onSurface = PaletaOscura.Tinta,
    surfaceVariant = PaletaOscura.SuperficieSuave,
    onSurfaceVariant = PaletaOscura.TintaSuave,
    surfaceContainerLowest = PaletaOscura.Fondo,
    surfaceContainerLow = PaletaOscura.Superficie,
    surfaceContainer = PaletaOscura.Superficie,
    surfaceContainerHigh = PaletaOscura.SuperficieSuave,
    surfaceContainerHighest = PaletaOscura.SuperficieSuave,
    error = PaletaOscura.RojoVencido,
    outline = PaletaOscura.BordeCampo,
    outlineVariant = PaletaOscura.Borde
)

/** [temaOscuro] viene de Configuración (se guarda en el teléfono). */
@Composable
fun KiloTheme(temaOscuro: Boolean = false, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTemaOscuro provides temaOscuro) {
        MaterialTheme(
            colorScheme = if (temaOscuro) EsquemaOscuro else EsquemaClaro,
            typography = TipografiaKilo,
            content = content
        )
    }
}