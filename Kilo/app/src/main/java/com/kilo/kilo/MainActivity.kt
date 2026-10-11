package com.kilo.kilo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kilo.kilo.ui.inicio.InicioPantalla
import com.kilo.kilo.ui.splash.SplashScreen
import com.kilo.kilo.ui.theme.KiloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiloTheme {
                // true = se muestra el Splash; pasa a false cuando termina de cargar
                var mostrarSplash by rememberSaveable { mutableStateOf(true) }

                if (mostrarSplash) {
                    SplashScreen(onTerminar = { mostrarSplash = false })
                } else {
                    InicioPantalla()
                }
            }
        }
    }
}