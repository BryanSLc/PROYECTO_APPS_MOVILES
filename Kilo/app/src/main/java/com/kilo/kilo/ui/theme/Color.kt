package com.kilo.kilo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** true cuando la app está en tema oscuro (lo provee KiloTheme). */
val LocalTemaOscuro = staticCompositionLocalOf { false }

// ---------------------------------------------------------------
//  Colores de marca: NO cambian con el tema (splash, logotipo)
// ---------------------------------------------------------------
val MoradoMarca = Color(0xFF6D28D9)
val Blanco = Color(0xFFFFFFFF)

// ---------------------------------------------------------------
//  Paletas. Cada color tiene su versión clara y su versión oscura.
// ---------------------------------------------------------------
internal object PaletaClara {
    val Morado = Color(0xFF6D28D9)
    val MoradoOscuro = Color(0xFF5B21B6)
    val MoradoSuave = Color(0xFFF5F2FE)
    val SobreMorado = Color(0xFFFFFFFF)

    val Tinta = Color(0xFF16161C)
    val TintaMedia = Color(0xFF4B4B57)
    val TintaSuave = Color(0xFF84848F)
    val TintaTenue = Color(0xFFA0A0AB)

    val Fondo = Color(0xFFF4F4F8)
    val Superficie = Color(0xFFFFFFFF)
    val SuperficieSuave = Color(0xFFF7F7FA)
    val Borde = Color(0xFFE2E2EA)
    val BordeCampo = Color(0xFF8E8E9C)

    val RojoVencido = Color(0xFFDC2626)
    val RojoVencidoFondo = Color(0xFFFDEAEA)
    val AmbarProximo = Color(0xFFB45309)
    val AmbarProximoFondo = Color(0xFFFDF3E3)
    val VerdeAlDia = Color(0xFF15803D)
    val VerdeAlDiaFondo = Color(0xFFE8F5EC)
    val GrisSinDatos = Color(0xFF8A8A97)
    val GrisSinDatosFondo = Color(0xFFF1F1F5)
}

internal object PaletaOscura {
    val Morado = Color(0xFFA78BFA)
    val MoradoOscuro = Color(0xFFDDD6FE)
    val MoradoSuave = Color(0xFF2E2350)
    val SobreMorado = Color(0xFF1F1147)

    val Tinta = Color(0xFFECECF1)
    val TintaMedia = Color(0xFFC9C9D3)
    val TintaSuave = Color(0xFFA1A1AD)
    val TintaTenue = Color(0xFF8A8A96)

    val Fondo = Color(0xFF0F0F14)
    val Superficie = Color(0xFF1B1B22)
    val SuperficieSuave = Color(0xFF24242C)
    val Borde = Color(0xFF3A3A45)
    val BordeCampo = Color(0xFF6B6B78)

    val RojoVencido = Color(0xFFF87171)
    val RojoVencidoFondo = Color(0xFF3B1A1C)
    val AmbarProximo = Color(0xFFFBBF24)
    val AmbarProximoFondo = Color(0xFF3A2B10)
    val VerdeAlDia = Color(0xFF4ADE80)
    val VerdeAlDiaFondo = Color(0xFF143222)
    val GrisSinDatos = Color(0xFFA1A1AD)
    val GrisSinDatosFondo = Color(0xFF2A2A33)
}

@Composable
private fun segunTema(claro: Color, oscuro: Color): Color =
    if (LocalTemaOscuro.current) oscuro else claro

// ---------------------------------------------------------------
//  Colores que usan las pantallas: se resuelven con el tema activo.
//  Se usan igual que antes (por ejemplo: color = Tinta), solo dentro de un @Composable.
// ---------------------------------------------------------------
val Morado: Color
    @Composable get() = segunTema(PaletaClara.Morado, PaletaOscura.Morado)
val MoradoSuave: Color
    @Composable get() = segunTema(PaletaClara.MoradoSuave, PaletaOscura.MoradoSuave)
/** Color del texto/icono que va SOBRE un fondo [Morado] (botones). */
val SobreMorado: Color
    @Composable get() = segunTema(PaletaClara.SobreMorado, PaletaOscura.SobreMorado)

val Tinta: Color
    @Composable get() = segunTema(PaletaClara.Tinta, PaletaOscura.Tinta)
val TintaMedia: Color
    @Composable get() = segunTema(PaletaClara.TintaMedia, PaletaOscura.TintaMedia)
val TintaSuave: Color
    @Composable get() = segunTema(PaletaClara.TintaSuave, PaletaOscura.TintaSuave)
val TintaTenue: Color
    @Composable get() = segunTema(PaletaClara.TintaTenue, PaletaOscura.TintaTenue)

val Fondo: Color
    @Composable get() = segunTema(PaletaClara.Fondo, PaletaOscura.Fondo)
val Superficie: Color
    @Composable get() = segunTema(PaletaClara.Superficie, PaletaOscura.Superficie)
val SuperficieSuave: Color
    @Composable get() = segunTema(PaletaClara.SuperficieSuave, PaletaOscura.SuperficieSuave)
val Borde: Color
    @Composable get() = segunTema(PaletaClara.Borde, PaletaOscura.Borde)
val BordeCampo: Color
    @Composable get() = segunTema(PaletaClara.BordeCampo, PaletaOscura.BordeCampo)

val RojoVencido: Color
    @Composable get() = segunTema(PaletaClara.RojoVencido, PaletaOscura.RojoVencido)
val RojoVencidoFondo: Color
    @Composable get() = segunTema(PaletaClara.RojoVencidoFondo, PaletaOscura.RojoVencidoFondo)
val AmbarProximo: Color
    @Composable get() = segunTema(PaletaClara.AmbarProximo, PaletaOscura.AmbarProximo)
val AmbarProximoFondo: Color
    @Composable get() = segunTema(PaletaClara.AmbarProximoFondo, PaletaOscura.AmbarProximoFondo)
val VerdeAlDia: Color
    @Composable get() = segunTema(PaletaClara.VerdeAlDia, PaletaOscura.VerdeAlDia)
val VerdeAlDiaFondo: Color
    @Composable get() = segunTema(PaletaClara.VerdeAlDiaFondo, PaletaOscura.VerdeAlDiaFondo)
val GrisSinDatos: Color
    @Composable get() = segunTema(PaletaClara.GrisSinDatos, PaletaOscura.GrisSinDatos)
val GrisSinDatosFondo: Color
    @Composable get() = segunTema(PaletaClara.GrisSinDatosFondo, PaletaOscura.GrisSinDatosFondo)
