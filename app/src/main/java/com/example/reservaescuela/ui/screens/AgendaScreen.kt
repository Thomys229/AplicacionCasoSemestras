package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaescuela.data.SolicitudNube
import com.example.reservaescuela.model.*
import com.example.reservaescuela.ui.AgendaViewModel
import com.example.reservaescuela.ui.components.*
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun AgendaScreen(volver: () -> Unit, vm: AgendaViewModel = viewModel()) {
    val prefs = LocalContext.current.getSharedPreferences("agenda_nube", 0)
    var url by rememberSaveable { mutableStateOf(prefs.getString("url", "").orEmpty()) }
    var usuario by rememberSaveable { mutableStateOf("docente") }
    var clave by remember { mutableStateOf("") }
    val ciclo = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(vm.usuario, ciclo) {
        if (vm.usuario.isNotEmpty()) {
            prefs.edit().putString("url", url.trim()).apply()
            clave = ""
            ciclo.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) { vm.actualizar(); delay(5000) }
            }
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize().systemBarsPadding(),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            TextButton(onClick = volver) { Text("Volver") }
            Text("Agenda compartida", style = MaterialTheme.typography.headlineSmall)
            if (vm.mensaje.isNotBlank()) Text(vm.mensaje, color = MaterialTheme.colorScheme.error)
            if (vm.estadoConexion.isNotBlank()) Text(vm.estadoConexion)
        }
        if (vm.usuario.isEmpty()) {
            item {
                AppTextField(url, { url = it }, "URL HTTPS de Render", enabled = !vm.enviando)
                DropdownField("Acceso", listOf("docente", "inspectoria"), usuario) { usuario = it }
                PasswordField(clave, { clave = it }, "Contraseña del servicio")
                Button(enabled = clave.isNotBlank() && !vm.enviando, onClick = { vm.conectar(url, usuario, clave) }) {
                    Text(if (vm.enviando) "Conectando…" else "Entrar a la agenda")
                }
            }
        } else {
            item { NuevaSolicitud(vm) }
            item { Text("Reservas · ${vm.usuario}", style = MaterialTheme.typography.titleMedium) }
            if (vm.turnos.isEmpty()) item { Text("Sin reservas compartidas.") }
            items(vm.turnos, key = { it.id }) { r ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${spaceName(r.espacio)} · ${r.estado}")
                        Text("${r.fecha} · ${r.inicio.take(5)}–${r.fin.take(5)}")
                        Text("${r.actividad} · ${r.personas} asistentes")
                        if (vm.usuario == "inspectoria" && r.estado == "Pendiente") {
                            Row {
                                TextButton(enabled = !vm.enviando, onClick = { vm.decidir(r.id, "Aceptada") }) { Text("Aceptar") }
                                TextButton(enabled = !vm.enviando, onClick = { vm.decidir(r.id, "Rechazada") }) { Text("Rechazar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NuevaSolicitud(vm: AgendaViewModel) {
    var espacio by rememberSaveable { mutableIntStateOf(1) }
    var fecha by rememberSaveable { mutableStateOf(nextBusinessDay().toString()) }
    var inicio by rememberSaveable { mutableStateOf("09:00") }
    var fin by rememberSaveable { mutableStateOf("10:00") }
    var actividad by rememberSaveable { mutableStateOf("") }
    var personas by rememberSaveable { mutableStateOf("1") }
    val id = rememberSaveable(espacio, fecha, inicio, fin, actividad, personas) { UUID.randomUUID().toString() }
    Column {
        SpaceDropdown(espacio) { espacio = it }
        DateField(fecha, { fecha = it }) { dia, hoy -> bookingDateError(dia, hoy) == null }
        AppTextField(inicio, { inicio = it }, "Inicio (HH:mm)")
        AppTextField(fin, { fin = it }, "Término (HH:mm)")
        AppTextField(actividad, { actividad = it.take(120) }, "Actividad", enabled = !vm.enviando)
        AppTextField(personas, { personas = it.filter(Char::isDigit).take(3) }, "Asistentes")
        Button(enabled = !vm.enviando && actividad.isNotBlank() && personas.toIntOrNull() != null, onClick = {
            vm.guardar(SolicitudNube(id, espacio, fecha, inicio, fin, personas.toInt(), actividad.trim())) { actividad = "" }
        }) { Text(if (vm.usuario == "inspectoria") "Confirmar reserva" else "Solicitar reserva") }
    }
}
