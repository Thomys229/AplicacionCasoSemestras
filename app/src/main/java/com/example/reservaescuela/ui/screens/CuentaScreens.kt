package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.components.AppTextField
import com.example.reservaescuela.ui.components.FormColumn
import com.example.reservaescuela.ui.components.PasswordField

// editar datos del usuario
@Composable
fun PerfilScreen(vm: AppViewModel, show: (String) -> Unit) {
    val user = vm.currentUser ?: return
    var name by remember { mutableStateOf(user.name) }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    FormColumn {
        AppTextField(user.email, {}, "Correo (no se puede cambiar)", enabled = false)
        AppTextField(name, { name = it; nameError = null }, "Nombre completo", nameError)
        Text("Deja las contraseñas vacías si no quieres cambiarla.", style = MaterialTheme.typography.bodySmall)
        PasswordField(password, { password = it; passwordError = null }, "Nueva contraseña", passwordError)
        PasswordField(confirm, { confirm = it; confirmError = null }, "Repetir nueva contraseña", confirmError)
        Button(
            onClick = {
                nameError = if (name.isBlank()) "Ingresa tu nombre" else null
                passwordError = if (password.isNotEmpty() && password.length < 6) "Debe tener al menos 6 caracteres" else null
                confirmError = if (confirm != password) "Las contraseñas no coinciden" else null
                if (nameError == null && passwordError == null && confirmError == null) {
                    val error = vm.updateProfile(name, password)
                    if (error == null) {
                        password = ""
                        confirm = ""
                        show("Datos actualizados.")
                    } else {
                        show(error)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }
    }
}

// vista de cierre de sesion
@Composable
fun LogoutScreen(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text("¿Quieres cerrar sesión?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Tus datos quedan guardados en este teléfono.")
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
            Button(onClick = onConfirm) { Text("Cerrar sesión") }
        }
    }
}
