package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.model.Block
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.nextBusinessDay
import com.example.reservaescuela.model.spaceName
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.components.AppTextField
import com.example.reservaescuela.ui.components.EmptyText
import com.example.reservaescuela.ui.components.FormColumn
import com.example.reservaescuela.ui.components.SpaceDropdown
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.reservaescuela.model.bookingDateError
import com.example.reservaescuela.model.canBlockDate
import com.example.reservaescuela.ui.components.DateField

// formulario para reservar un espacio
@Composable
fun ReservarScreen(vm: AppViewModel, show: (String) -> Unit, onSent: () -> Unit) {
    var space by remember { mutableIntStateOf(1) }
    var date by rememberSaveable { mutableStateOf(nextBusinessDay().toString()) }
    var start by remember { mutableStateOf("08:15") }
    var end by remember { mutableStateOf("09:00") }
    var activity by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }
    var resources by remember { mutableStateOf("Ninguno") }
    var workshop by remember { mutableStateOf(false) }
    val busy = vm.bookings.filter { it.space == space && it.date == date && it.status in listOf("Aceptada", "Confirmada") }
    val blocked = vm.blocks.filter { it.space == space && it.date == date }

    FormColumn {
        SpaceDropdown(space) { space = it }
        DateField(date, { date = it }) { day, today -> bookingDateError(day, today) == null }
        AppTextField(start, { start = it }, "Inicio (HH:mm)")
        AppTextField(end, { end = it }, "Término (HH:mm)")
        Row {
            Checkbox(checked = workshop, onCheckedChange = { workshop = it })
            Text("Taller (lunes a jueves 15:30–17:15)", Modifier.padding(top = 13.dp))
        }
        AppTextField(activity, { activity = it }, "Actividad")
        AppTextField(group, { group = it }, "Curso o grupo")
        AppTextField(people, { people = it.filter(Char::isDigit) }, "Asistentes", keyboardType = KeyboardType.Number)
        AppTextField(resources, { resources = it }, "Recursos requeridos")
        Text("Ocupación confirmada: " + busy.joinToString { "${it.start}–${it.end}" }.ifBlank { "Sin reservas" })
        Text("Bloqueos: " + blocked.joinToString { "${it.start}–${it.end}" }.ifBlank { "Sin bloqueos" })

        Button(
            onClick = {
                val booking = Booking(
                    id = System.currentTimeMillis(),
                    owner = "",
                    space = space,
                    date = date.trim(),
                    start = start.trim(),
                    end = end.trim(),
                    activity = activity.trim(),
                    group = group.trim(),
                    people = people.toIntOrNull() ?: 0,
                    resources = resources.trim(),
                    workshop = workshop
                )
                val error = vm.sendBooking(booking)
                if (error != null) {
                    show(error)
                } else {
                    show("Solicitud enviada a Inspectoría.")
                    onSent()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar solicitud")
        }
    }
}

// lista de reservas del usuario
@Composable
fun MisReservasScreen(vm: AppViewModel, show: (String) -> Unit) {
    val user = vm.currentUser ?: return
    val mine = vm.bookings.filter { it.owner == user.email }.sortedByDescending { it.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (mine.isEmpty()) {
            item { EmptyText("Aún no tienes reservas.") }
        }
        items(mine, key = { it.id }) { booking ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(spaceName(booking.space), style = MaterialTheme.typography.titleMedium)
                    Text("${booking.date} · ${booking.start}–${booking.end}")
                    Text("${booking.activity} · ${booking.status}")
                    Text("${booking.group} · ${booking.people} asistentes · ${booking.resources}")
                    if (booking.status == "Pendiente") {
                        TextButton(onClick = {
                            vm.cancelBooking(booking.id)
                            show("Reserva cancelada.")
                        }) {
                            Text("Cancelar reserva")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudesScreen(vm: AppViewModel, show: (String) -> Unit) {
    if (!vm.isAdmin) { EmptyText("Acceso reservado a Inspectoría."); return }
    val all = vm.bookings.sortedByDescending { it.id }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (all.isEmpty()) item { EmptyText("No hay solicitudes todavía.") }
        items(all, key = { it.id }) { booking ->
            var reason by remember(booking.id) { mutableStateOf("") }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(spaceName(booking.space), style = MaterialTheme.typography.titleMedium)
                    Text("${booking.date} · ${booking.start}–${booking.end} · ${booking.status}")
                    Text("${booking.activity} · ${booking.group} · ${booking.people} asistentes")
                    Text("Solicitante: ${booking.owner}")
                    if (booking.reason.isNotBlank()) Text("Motivo: ${booking.reason}")
                    if (booking.status == "Pendiente") {
                        AppTextField(reason, { reason = it }, "Justificación del rechazo")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { show(vm.acceptBooking(booking.id) ?: "Reserva aceptada.") }) {
                                Text("Aceptar")
                            }
                            OutlinedButton(onClick = { show(vm.rejectBooking(booking.id, reason) ?: "Solicitud rechazada.") }) {
                                Text("Rechazar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BloqueosScreen(vm: AppViewModel, show: (String) -> Unit) {
    if (!vm.isAdmin) { EmptyText("Acceso reservado a Inspectoría."); return }
    var space by remember { mutableIntStateOf(1) }
    var date by rememberSaveable { mutableStateOf("") }
    var start by remember { mutableStateOf("08:15") }
    var end by remember { mutableStateOf("09:00") }
    var reason by remember { mutableStateOf("") }
    FormColumn {
        SpaceDropdown(space) { space = it }
        DateField(date, { date = it }, ::canBlockDate)
        AppTextField(start, { start = it }, "Inicio (HH:mm)")
        AppTextField(end, { end = it }, "Término (HH:mm)")
        AppTextField(reason, { reason = it }, "Mantenimiento o evento")
        Button(
            onClick = {
                val block = Block(System.currentTimeMillis(), space, date.trim(), start.trim(), end.trim(), reason.trim())
                show(vm.addBlock(block) ?: "Bloqueo registrado.")
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar bloqueo") }
        Text("Bloqueos activos", style = MaterialTheme.typography.titleMedium)
        if (vm.blocks.isEmpty()) EmptyText("No hay bloqueos.")
        vm.blocks.forEach { block ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${spaceName(block.space)} · ${block.date}")
                    Text("${block.start}–${block.end} · ${block.reason}")
                    TextButton(onClick = { vm.removeBlock(block.id); show("Bloqueo eliminado.") }) {
                        Text("Quitar bloqueo")
                    }
                }
            }
        }
    }
}
