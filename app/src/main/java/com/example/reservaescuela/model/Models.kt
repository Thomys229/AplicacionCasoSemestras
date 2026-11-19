package com.example.reservaescuela.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class User(
    val email: String,
    val name: String,
    val passwordHash: String,
    val role: String = "Docente"
)

data class Space(val id: Int, val name: String, val detail: String, val capacity: Int)

data class Booking(
    val id: Long,
    val owner: String,
    val space: Int,
    val date: String,
    val start: String,
    val end: String,
    val activity: String,
    val group: String = "",
    val people: Int = 0,
    val resources: String = "Ninguno",
    val workshop: Boolean = false,
    val status: String = "Pendiente",
    val reason: String = ""
)

data class Block(val id: Long, val space: Int, val date: String, val start: String, val end: String, val reason: String)
data class Ticket(
    val id: Long,
    val owner: String,
    val space: Int,
    val location: String,
    val severity: String,
    val description: String,
    val fileUri: String = "",
    val fileName: String = "",
    val fileType: String = ""
)
data class Notice(val email: String, val message: String, val created: Long)

// archivo pdf o word que sube el usuario
data class Document(
    val id: Long,
    val owner: String,
    val name: String,
    val type: String,
    val uri: String
)

val roles = listOf("Docente", "UTP", "Dirección", "PIE", "Inspectoría")

val spaces = listOf(
    Space(1, "Laboratorio de computación", "Equipos y proyector", 35),
    Space(2, "Laboratorio de ciencias", "Mesones de experimentación", 32),
    Space(3, "Biblioteca", "Lectura y estudio", 45),
    Space(4, "Sala sensorial", "Apoyo y acompañamiento PIE", 12),
    Space(5, "Centro de Innovación CIEMP", "Proyectos colaborativos", 40),
    Space(6, "Sala de reuniones", "Reuniones de equipos", 20),
    Space(7, "Huerto escolar", "Aprendizaje al aire libre", 30)
)

fun spaceName(id: Int): String = spaces.firstOrNull { it.id == id }?.name ?: "Espacio"

private fun isWeekend(date: LocalDate): Boolean =
    date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

fun nextBusinessDay(): LocalDate {
    var day = LocalDate.now().plusDays(1)
    while (isWeekend(day)) day = day.plusDays(1)
    return day
}

fun overlap(startA: String, endA: String, startB: String, endB: String): Boolean =
    startA < endB && startB < endA

fun isValidTimeRange(start: String, end: String): Boolean {
    val startTime = runCatching { LocalTime.parse(start) }.getOrNull() ?: return false
    val endTime = runCatching { LocalTime.parse(end) }.getOrNull() ?: return false
    return startTime.isBefore(endTime)
}

fun validateBooking(b: Booking, bookings: List<Booking>, blocks: List<Block>): String? {
    if (b.activity.isBlank() || b.group.isBlank() || b.resources.isBlank() || b.people <= 0)
        return "Completa actividad, grupo, asistentes y recursos."
    val capacity = spaces.firstOrNull { it.id == b.space }?.capacity ?: 0
    if (b.people > capacity) return "La cantidad supera la capacidad del espacio."
    val date = runCatching { LocalDate.parse(b.date) }.getOrNull() ?: return "Fecha inválida: usa AAAA-MM-DD."
    val start = runCatching { LocalTime.parse(b.start) }.getOrNull() ?: return "Hora de inicio inválida: usa HH:mm."
    val end = runCatching { LocalTime.parse(b.end) }.getOrNull() ?: return "Hora de término inválida: usa HH:mm."
    if (!start.isBefore(end)) return "La hora de inicio debe ser anterior al término."
    if (isWeekend(date)) return "No se permiten reservas los fines de semana."
    if (date.isBefore(nextBusinessDay())) return "Debes reservar con al menos un día hábil de anticipación."
    if (start.isBefore(LocalTime.of(8, 15))) return "La jornada comienza a las 08:15."
    if (date.dayOfWeek == DayOfWeek.FRIDAY) {
        if (end.isAfter(LocalTime.of(14, 0))) return "El viernes termina a las 14:00."
    } else if (b.workshop) {
        if (start.isBefore(LocalTime.of(15, 30)) || end.isAfter(LocalTime.of(17, 15)))
            return "Los talleres funcionan de 15:30 a 17:15."
    } else if (end.isAfter(LocalTime.of(15, 30))) return "La jornada académica termina a las 15:30."
    if (blocks.any { it.space == b.space && it.date == b.date && overlap(it.start, it.end, b.start, b.end) })
        return "Horario bloqueado por Inspectoría."
    if (bookings.any { it.id != b.id && it.space == b.space && it.date == b.date && it.status in listOf("Aceptada", "Confirmada") && overlap(it.start, it.end, b.start, b.end) })
        return "El espacio ya está reservado en ese horario."
    return null
}
