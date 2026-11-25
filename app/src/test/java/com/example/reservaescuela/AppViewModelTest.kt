package com.example.reservaescuela

import android.app.Application
import android.content.Context
import com.example.reservaescuela.model.Block
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.Document
import com.example.reservaescuela.model.Ticket
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.utils.hashPassword
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppViewModelTest {

    private lateinit var vm: AppViewModel

    @Before
    fun preparar() {
        val app: Application = RuntimeEnvironment.getApplication()
        app.getSharedPreferences("escuela_marcela_paz_v3", Context.MODE_PRIVATE)
            .edit().clear().commit()
        vm = AppViewModel(app)
    }

    private fun fecha(): String = LocalDate.now()
        .plusWeeks(3)
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        .toString()

    private fun registrarDocente() {
        vm.register("Ana", "ana@escuela.cl", "123456", "Docente")
    }

    private fun registrarInspector() {
        vm.register("Luis", "inspector@escuela.cl", "123456", "Inspectoría")
    }

    private fun reserva() = Booking(
        id = 1L,
        owner = "",
        space = 1,
        date = fecha(),
        start = "09:00",
        end = "10:00",
        activity = "Clase de tecnología",
        group = "7° básico",
        people = 25,
        resources = "Proyector"
    )

    private fun bloqueo() = Block(
        id = 1L,
        space = 1,
        date = fecha(),
        start = "09:00",
        end = "10:00",
        reason = "Mantenimiento"
    )

    private fun incidencia() = Ticket(
        id = 1L,
        owner = "",
        space = 1,
        location = "Laboratorio",
        severity = "Leve",
        description = "Proyector sin señal",
        fileUri = "content://prueba/1",
        fileName = "foto.jpg",
        fileType = "image/jpeg"
    )

    private fun documento() = Document(
        id = 1L,
        owner = "",
        name = "Solicitud.pdf",
        type = "application/pdf",
        uri = "content://prueba/2"
    )

    @Test
    fun registraUsuarioConSuRol() {
        assertNull(vm.register("Ana", " ANA@ESCUELA.CL ", "123456", "Docente"))
        assertEquals("ana@escuela.cl", vm.currentUser?.email)
        assertEquals("Docente", vm.currentUser?.role)
    }

    @Test
    fun iniciaSesionConCredencialesCorrectas() {
        registrarDocente()
        vm.logout()
        assertNull(vm.login("ana@escuela.cl", "123456"))
        assertEquals("ana@escuela.cl", vm.sessionEmail)
    }

    @Test
    fun cierraSesion() {
        registrarDocente()
        vm.logout()
        assertEquals("", vm.sessionEmail)
        assertNull(vm.currentUser)
    }

    @Test
    fun actualizaNombreYContrasena() {
        registrarDocente()
        assertNull(vm.updateProfile("Ana María", "654321"))
        assertEquals("Ana María", vm.currentUser?.name)
        assertEquals(hashPassword("654321"), vm.currentUser?.passwordHash)
    }

    @Test
    fun enviaSolicitudDeReserva() {
        registrarDocente()
        assertNull(vm.sendBooking(reserva()))
        assertEquals(1, vm.bookings.size)
        assertEquals("ana@escuela.cl", vm.bookings.first().owner)
        assertEquals("Pendiente", vm.bookings.first().status)
    }

    @Test
    fun cancelaUnaReserva() {
        registrarDocente()
        vm.sendBooking(reserva())
        vm.cancelBooking(1L)
        assertEquals("Cancelada", vm.bookings.first().status)
    }

    @Test
    fun aceptaUnaSolicitudPendiente() {
        registrarDocente()
        vm.sendBooking(reserva())
        registrarInspector()
        assertNull(vm.acceptBooking(1L))
        assertEquals("Aceptada", vm.bookings.first().status)
        assertTrue(vm.notices.any { it.email == "ana@escuela.cl" })
    }

    @Test
    fun rechazaUnaSolicitudConMotivo() {
        registrarDocente()
        vm.sendBooking(reserva())
        registrarInspector()
        assertNull(vm.rejectBooking(1L, "Sala en reparación"))
        assertEquals("Rechazada", vm.bookings.first().status)
        assertEquals("Sala en reparación", vm.bookings.first().reason)
    }

    @Test
    fun registraBloqueoDeHorario() {
        registrarInspector()
        assertNull(vm.addBlock(bloqueo()))
        assertEquals(1, vm.blocks.size)
        assertEquals("Mantenimiento", vm.blocks.first().reason)
    }

    @Test
    fun eliminaBloqueoDeHorario() {
        registrarInspector()
        vm.addBlock(bloqueo())
        vm.removeBlock(1L)
        assertTrue(vm.blocks.isEmpty())
    }

    @Test
    fun registraIncidencia() {
        registrarDocente()
        vm.addTicket(incidencia())
        assertEquals(1, vm.tickets.size)
        assertEquals("ana@escuela.cl", vm.tickets.first().owner)
    }

    @Test
    fun eliminaAdjuntoDeIncidencia() {
        registrarDocente()
        vm.addTicket(incidencia())
        vm.removeTicketFile(1L)
        assertEquals("", vm.tickets.first().fileUri)
        assertEquals("", vm.tickets.first().fileName)
    }

    @Test
    fun agregaDocumento() {
        registrarDocente()
        vm.addDocument(documento())
        assertEquals(1, vm.documents.size)
        assertEquals("ana@escuela.cl", vm.documents.first().owner)
    }

    @Test
    fun eliminaDocumento() {
        registrarDocente()
        vm.addDocument(documento())
        vm.removeDocument(1L)
        assertTrue(vm.documents.isEmpty())
    }
}
