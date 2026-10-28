package com.example.reservaescuela.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.model.spaces
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.navigation.Routes

// vista de bienvenida despues de iniciar sesion
@Composable
fun HomeScreen(vm: AppViewModel, onNavigate: (String) -> Unit) {
    val user = vm.currentUser ?: return
    val myBookings = vm.bookings.count { it.owner == user.email && it.status == "Confirmada" }
    val myDocuments = vm.documents.count { it.owner == user.email }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Hola, ${user.name}", style = MaterialTheme.typography.headlineSmall)
                    Text(user.email, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(myBookings, "Reservas")
                StatCard(myDocuments, "Documentos")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onNavigate(Routes.RESERVAR) }, modifier = Modifier.weight(1f)) {
                    Text("Nueva reserva")
                }
                OutlinedButton(onClick = { onNavigate(Routes.DOCUMENTOS) }, modifier = Modifier.weight(1f)) {
                    Text("Mis documentos")
                }
            }
        }
        item {
            Text("Espacios de la escuela", style = MaterialTheme.typography.titleMedium)
        }
        items(spaces) { space ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(space.name, style = MaterialTheme.typography.titleMedium)
                    Text(space.detail)
                    Text("Capacidad: ${space.capacity} personas")
                }
            }
        }
    }
}

// el numero sube de 0 al valor real (animacion)
@Composable
private fun RowScope.StatCard(value: Int, label: String) {
    var target by remember { mutableStateOf(0) }
    LaunchedEffect(value) { target = value }
    val shown by animateIntAsState(targetValue = target, animationSpec = tween(800), label = "contador")
    Card(modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(12.dp)) {
            Text("$shown", style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
