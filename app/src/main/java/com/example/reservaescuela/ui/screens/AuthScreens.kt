package com.example.reservaescuela.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.components.AppTextField
import com.example.reservaescuela.ui.components.AuthLayout
import com.example.reservaescuela.ui.components.PasswordField

// pantalla de bienvenida (antes de iniciar sesion)
@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Escuela Escritora Marcela Paz",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Reserva de espacios, documentos e incidencias",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Iniciar sesión")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrarme")
                }
            }
        }
    }
}

@Composable
fun LoginScreen(vm: AppViewModel, onBack: () -> Unit, onGoRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf("") }

    AuthLayout(title = "Iniciar sesión", onBack = onBack) {
        AppTextField(
            value = email,
            onChange = { email = it; emailError = null; formError = "" },
            label = "Correo electrónico",
            error = emailError,
            keyboardType = KeyboardType.Email
        )
        PasswordField(
            value = password,
            onChange = { password = it; passwordError = null; formError = "" },
            label = "Contraseña",
            error = passwordError
        )
        if (formError.isNotBlank()) {
            Text(formError, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                emailError = if (Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) null else "Ingresa un correo válido"
                passwordError = if (password.isBlank()) "Ingresa tu contraseña" else null
                if (emailError == null && passwordError == null) {
                    formError = vm.login(email, password) ?: ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }
        TextButton(onClick = onGoRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

@Composable
fun RegisterScreen(vm: AppViewModel, onBack: () -> Unit, onGoLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    AuthLayout(title = "Crear cuenta", onBack = onBack) {
        AppTextField(name, { name = it; nameError = null }, "Nombre completo", nameError)
        AppTextField(
            email,
            { email = it; emailError = null },
            "Correo electrónico",
            emailError,
            KeyboardType.Email
        )
        PasswordField(password, { password = it; passwordError = null }, "Contraseña (mínimo 6)", passwordError)
        PasswordField(confirm, { confirm = it; confirmError = null }, "Repetir contraseña", confirmError)
        Button(
            onClick = {
                nameError = if (name.isBlank()) "Ingresa tu nombre" else null
                emailError = if (Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) null else "Ingresa un correo válido"
                passwordError = if (password.length < 6) "Debe tener al menos 6 caracteres" else null
                confirmError = if (confirm != password) "Las contraseñas no coinciden" else null
                val hasErrors = listOf(nameError, emailError, passwordError, confirmError).any { it != null }
                if (!hasErrors) {
                    val error = vm.register(name, email, password)
                    if (error != null) emailError = error
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarme")
        }
        TextButton(onClick = onGoLogin) {
            Text("Ya tengo cuenta")
        }
    }
}
