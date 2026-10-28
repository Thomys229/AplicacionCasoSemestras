package com.example.reservaescuela.model

data class User(
    val email: String,
    val name: String,
    val passwordHash: String
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
    val status: String = "Confirmada"
)

// archivo pdf o word que sube el usuario
data class Document(
    val id: Long,
    val owner: String,
    val name: String,
    val type: String,
    val uri: String
)

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
