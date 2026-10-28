package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.spaceName
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.components.AppTextField
import com.example.reservaescuela.ui.components.EmptyText
import com.example.reservaescuela.ui.components.FormColumn
import com.example.reservaescuela.ui.components.SpaceDropdown
import java.time.LocalDate

// formulario para reservar un espacio
@Composable
fun ReservarScreen(vm: AppViewModel, show: (String) -> Unit, onSent: () -> Unit) {
    var space by remember { mutableIntStateOf(1) }
    var date by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var start by remember { mutableStateOf("08:15") }
    var end by remember { mutableStateOf("09:00") }
    var activity by remember { mutableStateOf("") }

    FormColumn {
        SpaceDropdown(space) { space = it }
        AppTextField(date, { date = it }, "Fecha (AAAA-MM-DD)")
        AppTextField(start, { start = it }, "Inicio (HH:mm)")
        AppTextField(end, { end = it }, "Término (HH:mm)")
        AppTextField(activity, { activity = it }, "Actividad")

        Button(
            onClick = {
                val booking = Booking(
                    id = System.currentTimeMillis(),
                    owner = "",
                    space = space,
                    date = date.trim(),
                    start = start.trim(),
                    end = end.trim(),
                    activity = activity.trim()
                )
                val error = vm.sendBooking(booking)
                if (error != null) {
                    show(error)
                } else {
                    show("Reserva guardada.")
                    onSent()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reservar")
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
                    if (booking.status == "Confirmada") {
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
