package com.kilo.kilo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kilo.kilo.ui.pantallas.InicioPantalla
import com.kilo.kilo.ui.theme.KiloTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiloTheme {
                InicioPantalla()
            }
        }
    }
}