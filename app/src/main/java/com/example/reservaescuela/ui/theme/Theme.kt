package com.example.reservaescuela.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colores = lightColorScheme(
    primary = Color(0xFF176B62),
    secondary = Color(0xFF416B83),
    tertiary = Color(0xFFB86C3C),
    background = Color(0xFFF4F8F7),
    surface = Color.White
)

@Composable
fun ReservaEscuelaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colores, content = content)
}
