package com.example.reservaescuela.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.reservaescuela.data.LocalStore
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.Document
import com.example.reservaescuela.model.User
import com.example.reservaescuela.utils.hashPassword
import java.time.LocalDate
import java.time.LocalTime

// aqui vive todo el estado de la app. las pantallas solo lo muestran
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = LocalStore(application)

    // cuando estas listas cambian, las pantallas se redibujan solas
    var users by mutableStateOf(store.users())
        private set
    var bookings by mutableStateOf(store.bookings())
        private set
    var documents by mutableStateOf(store.documents())
        private set
    var sessionEmail by mutableStateOf(store.session())
        private set

    val currentUser: User?
        get() = users.firstOrNull { it.email == sessionEmail }

    // ---------- sesion ----------

    // todas las funciones devuelven el mensaje de error, o null si salio bien
    fun register(name: String, email: String, password: String): String? {
        val mail = email.trim().lowercase()
        if (users.any { it.email == mail }) return "Ese correo ya está registrado."
        users = users + User(mail, name.trim(), hashPassword(password))
        store.saveUsers(users)
        sessionEmail = mail
        store.saveSession(mail)
        return null
    }

    fun login(email: String, password: String): String? {
        val mail = email.trim().lowercase()
        val hash = hashPassword(password)
        val found = users.any { it.email == mail && it.passwordHash == hash }
        if (!found) return "Correo o contraseña incorrectos."
        sessionEmail = mail
        store.saveSession(mail)
        return null
    }

    fun logout() {
        sessionEmail = ""
        store.saveSession("")
    }

    fun updateProfile(name: String, newPassword: String): String? {
        val me = currentUser ?: return "No hay sesión activa."
        val hash = if (newPassword.isBlank()) me.passwordHash else hashPassword(newPassword)
        val updated = me.copy(name = name.trim(), passwordHash = hash)
        users = users.map { if (it.email == me.email) updated else it }
        store.saveUsers(users)
        return null
    }

    // ---------- reservas ----------

    fun sendBooking(booking: Booking): String? {
        val me = currentUser ?: return "No hay sesión activa."

        if (booking.activity.isBlank()) return "Escribe la actividad."

        val date = runCatching { LocalDate.parse(booking.date) }.getOrNull()
            ?: return "Fecha inválida: usa AAAA-MM-DD."
        if (date.isBefore(LocalDate.now())) return "La fecha no puede ser pasada."

        val start = runCatching { LocalTime.parse(booking.start) }.getOrNull()
            ?: return "Hora de inicio inválida: usa HH:mm."
        val end = runCatching { LocalTime.parse(booking.end) }.getOrNull()
            ?: return "Hora de término inválida: usa HH:mm."
        if (!start.isBefore(end)) return "La hora de inicio debe ser anterior al término."

        bookings = bookings + booking.copy(owner = me.email)
        store.saveBookings(bookings)
        return null
    }

    fun cancelBooking(id: Long) {
        bookings = bookings.map { if (it.id == id) it.copy(status = "Cancelada") else it }
        store.saveBookings(bookings)
    }

    // ---------- documentos ----------

    fun addDocument(document: Document) {
        val me = currentUser ?: return
        documents = documents + document.copy(owner = me.email)
        store.saveDocuments(documents)
    }

    fun removeDocument(id: Long) {
        documents = documents.filterNot { it.id == id }
        store.saveDocuments(documents)
    }
}
