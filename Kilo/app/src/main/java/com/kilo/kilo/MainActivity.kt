package com.kilo.kilo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kilo.kilo.ui.agregar_vehiculo.AgregarVehiculoScreen
import com.kilo.kilo.ui.configuracion.ConfiguracionScreen
import com.kilo.kilo.ui.historial.HistorialScreen
import com.kilo.kilo.ui.inicio.InicioPantalla
import com.kilo.kilo.ui.mantenimiento.DetalleMantenimientoScreen
import com.kilo.kilo.ui.mis_vehiculos.MisVehiculosScreen
import com.kilo.kilo.ui.splash.SplashScreen
import com.kilo.kilo.ui.theme.Fondo
import com.kilo.kilo.ui.theme.KiloTheme
import com.kilo.kilo.ui.theme.Morado
import com.kilo.kilo.ui.theme.MoradoSuave
import com.kilo.kilo.ui.theme.Superficie
import com.kilo.kilo.ui.theme.TintaTenue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiloTheme {
                KiloNavHost()
            }
        }
    }
}

/** Nombres de las rutas (la "dirección" de cada pantalla). */
private object Rutas {
    const val SPLASH = "splash"
    const val INICIO = "inicio"
    const val DETALLE = "detalle_mantenimiento/{tipo}"
    const val HISTORIAL = "historial"
    const val VEHICULOS = "mis_vehiculos"
    const val AGREGAR_VEHICULO = "agregar_vehiculo"
    const val AJUSTES = "configuracion"

    /** Ruta del Detalle de una pieza concreta, por ejemplo detalle_mantenimiento/FILTRO */
    fun detalle(clave: String) = "detalle_mantenimiento/$clave"
}

/** Pestañas de la barra inferior. */
private data class Tab(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val TABS = listOf(
    Tab(Rutas.INICIO, "Inicio", Icons.Rounded.Home),
    Tab(Rutas.VEHICULOS, "Vehículos", Icons.Rounded.DirectionsCar),
    Tab(Rutas.AJUSTES, "Ajustes", Icons.Rounded.Settings)
)

/** Cambia de pestaña sin ir apilando pantallas: siempre se vuelve a Inicio al retroceder. */
private fun irATab(navController: NavHostController, ruta: String) {
    if (ruta == Rutas.INICIO) {
        navController.popBackStack(Rutas.INICIO, inclusive = false)
    } else {
        navController.navigate(ruta) {
            popUpTo(Rutas.INICIO)
            launchSingleTop = true
        }
    }
}

/** Un número de pestaña (0 = Inicio, 1 = Vehículos, 2 = Ajustes) a su ruta. */
private fun irAPestana(navController: NavHostController, indice: Int) {
    irATab(navController, TABS[indice].ruta)
}

/** Mapa de navegación: qué pantalla se muestra en cada ruta y cómo se pasa de una a otra. */
@Composable
private fun KiloNavHost() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        containerColor = Fondo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Mis vehículos y Configuración no traen barra propia: se les pone la compartida
            if (rutaActual == Rutas.VEHICULOS || rutaActual == Rutas.AJUSTES) {
                BarraTabs(
                    rutaActual = rutaActual,
                    onTabClick = { ruta -> irATab(navController, ruta) }
                )
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = Rutas.SPLASH,
            modifier = Modifier.padding(relleno)
        ) {

            composable(Rutas.SPLASH) {
                SplashScreen(
                    onTerminar = {
                        // Pasa al Inicio y borra el Splash para que "atrás" no regrese a él
                        navController.navigate(Rutas.INICIO) {
                            popUpTo(Rutas.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Rutas.INICIO) {
                InicioPantalla(
                    onVerHistorial = { navController.navigate(Rutas.HISTORIAL) },
                    onCambiarVehiculo = { irATab(navController, Rutas.VEHICULOS) },
                    onIrAVehiculos = { irATab(navController, Rutas.VEHICULOS) },
                    onIrAAjustes = { irATab(navController, Rutas.AJUSTES) },
                    onPiezaClick = { pieza -> navController.navigate(Rutas.detalle(pieza.clave)) }
                )
            }

            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("tipo") { type = NavType.StringType })
            ) {
                DetalleMantenimientoScreen(
                    onVolver = { navController.popBackStack() },
                    onPestanaClick = { indice -> irAPestana(navController, indice) }
                )
            }

            composable(Rutas.HISTORIAL) {
                HistorialScreen(onVolver = { navController.popBackStack() })
            }

            composable(Rutas.VEHICULOS) {
                MisVehiculosScreen(
                    onAgregar = { navController.navigate(Rutas.AGREGAR_VEHICULO) }
                )
            }

            composable(Rutas.AGREGAR_VEHICULO) {
                AgregarVehiculoScreen(onVolver = { navController.popBackStack() })
            }

            composable(Rutas.AJUSTES) {
                ConfiguracionScreen()
            }
        }
    }
}

/** Barra inferior compartida (la misma del Inicio). */
@Composable
private fun BarraTabs(rutaActual: String?, onTabClick: (String) -> Unit) {
    NavigationBar(containerColor = Superficie) {
        TABS.forEach { tab ->
            NavigationBarItem(
                selected = tab.ruta == rutaActual,
                onClick = { onTabClick(tab.ruta) },
                icon = { Icon(tab.icono, contentDescription = tab.etiqueta) },
                label = { Text(tab.etiqueta, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Morado,
                    selectedTextColor = Morado,
                    unselectedIconColor = TintaTenue,
                    unselectedTextColor = TintaTenue,
                    indicatorColor = MoradoSuave
                )
            )
        }
    }
}