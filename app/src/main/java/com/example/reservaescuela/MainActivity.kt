package com.example.reservaescuela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.reservaescuela.ui.navigation.AppNavigation
import com.example.reservaescuela.ui.theme.ReservaEscuelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReservaEscuelaTheme {
                AppNavigation()
            }
        }
    }
}
