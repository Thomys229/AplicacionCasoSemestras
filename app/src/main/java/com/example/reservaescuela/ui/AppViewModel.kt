package com.example.reservaescuela.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.reservaescuela.data.LocalStore
import com.example.reservaescuela.model.Block
import com.example.reservaescuela.model.Notice
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.validateBooking
import com.example.reservaescuela.model.isValidTimeRange
import com.example.reservaescuela.model.overlap
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
    var blocks by mutableStateOf(store.blocks())
        private set
    var notices by mutableStateOf(store.notices())
        private set
    var documents by mutableStateOf(store.documents())
        private set
    var sessionEmail by mutableStateOf(store.session())
        private set

    val currentUser: User?
        get() = users.firstOrNull { it.email == sessionEmail }

    val isAdmin: Boolean
        get() = currentUser?.role == "Inspectoría"

    // ---------- sesion ----------

    // todas las funciones devuelven el mensaje de error, o null si salio bien
    fun register(name: String, email: String, password: String, role: String): String? {
        val mail = email.trim().lowercase()
        if (users.any { it.email == mail }) return "Ese correo ya está registrado."
        users = users + User(mail, name.trim(), hashPassword(password), role)
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
        val error = validateBooking(booking, bookings, blocks)
        if (error != null) return error
        bookings = bookings + booking.copy(owner = me.email)
        store.saveBookings(bookings)
        users.filter { it.role == "Inspectoría" }.forEach {
            addNotice(it.email, "Nueva solicitud de ${me.name} para ${booking.date}")
        }
        return null
    }

    private fun addNotice(email: String, message: String) {
        notices = notices + Notice(email, message, System.currentTimeMillis())
        store.saveNotices(notices)
    }

    fun cancelBooking(id: Long) {
        bookings = bookings.map { if (it.id == id) it.copy(status = "Cancelada") else it }
        store.saveBookings(bookings)
    }


    // ---------- administración de reservas ----------
    fun acceptBooking(id: Long): String? {
        if (!isAdmin) return "Solo Inspectoría puede aprobar solicitudes."
        val booking = bookings.firstOrNull { it.id == id && it.status == "Pendiente" } ?: return "La solicitud no está pendiente."
        val error = validateBooking(booking, bookings, blocks)
        if (error != null) return error
        bookings = bookings.map { if (it.id == id) it.copy(status = "Aceptada") else it }
        store.saveBookings(bookings)
        addNotice(booking.owner, "Tu solicitud del ${booking.date} fue aceptada")
        return null
    }

    fun rejectBooking(id: Long, reason: String): String? {
        if (!isAdmin) return "Solo Inspectoría puede rechazar solicitudes."
        if (reason.isBlank()) return "La justificación es obligatoria."
        val booking = bookings.firstOrNull { it.id == id && it.status == "Pendiente" } ?: return "La solicitud no está pendiente."
        bookings = bookings.map { if (it.id == id) it.copy(status = "Rechazada", reason = reason.trim()) else it }
        store.saveBookings(bookings)
        addNotice(booking.owner, "Tu solicitud del ${booking.date} fue rechazada: ${reason.trim()}")
        return null
    }

    fun addBlock(block: Block): String? {
        if (!isAdmin) return "Solo Inspectoría puede bloquear horarios."
        if (block.reason.isBlank() || block.date.isBlank() || block.start.isBlank() || block.end.isBlank())
            return "Completa todos los campos."
        if (runCatching { LocalDate.parse(block.date) }.isFailure || !isValidTimeRange(block.start, block.end))
            return "Fecha u horario inválidos."
        if (bookings.any { it.space == block.space && it.date == block.date &&
            it.status in listOf("Aceptada", "Confirmada") && overlap(it.start, it.end, block.start, block.end) })
            return "Hay una reserva aceptada en ese horario."
        blocks = blocks + block
        store.saveBlocks(blocks)
        return null
    }

    fun removeBlock(id: Long) {
        if (!isAdmin) return
        blocks = blocks.filterNot { it.id == id }
        store.saveBlocks(blocks)
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
