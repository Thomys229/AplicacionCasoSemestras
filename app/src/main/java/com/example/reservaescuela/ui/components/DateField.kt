package com.example.reservaescuela.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// El calendario representa cada día a medianoche UTC.
private fun calendarDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(value: String, onChange: (String) -> Unit, allowed: (LocalDate, LocalDate) -> Boolean) {
    var open by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Text(if (value.isEmpty()) "Seleccionar fecha" else "Fecha: $value")
    }
    if (!open) return

    val today = LocalDate.now()
    val selected = value.takeIf { it.isNotEmpty() }?.let(LocalDate::parse)?.takeIf { allowed(it, today) }
    val dates = remember(today, allowed) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = allowed(calendarDate(utcTimeMillis), today)
            override fun isSelectableYear(year: Int) = year >= today.year
        }
    }
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = selected?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = dates
    )
    val choice = picker.selectedDateMillis?.let(::calendarDate)
    DatePickerDialog(
        onDismissRequest = { open = false },
        confirmButton = {
            TextButton(enabled = choice != null && allowed(choice, today), onClick = {
                if (choice != null && allowed(choice, LocalDate.now())) {
                    onChange(choice.toString())
                }
                open = false
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar") } }
    ) {
        DatePicker(state = picker, showModeToggle = false)
    }
}
