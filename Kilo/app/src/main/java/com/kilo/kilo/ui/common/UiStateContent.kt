package com.kilo.kilo.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kilo.kilo.ui.theme.Morado
import com.kilo.kilo.ui.theme.RojoVencido

/**
 * Dibuja Loading y Error; cuando hay datos, llama a [content] con ellos.
 * [content] es una vista sin estado: solo recibe datos inmutables.
 */
@Composable
fun <T> UiStateContent(
    uiState: UiState<T>,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (T) -> Unit
) {
    when (uiState) {
        is UiState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Morado)
        }

        is UiState.Error -> Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                text = uiState.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = RojoVencido,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }

        is UiState.Success -> content(uiState.data)
    }
}
