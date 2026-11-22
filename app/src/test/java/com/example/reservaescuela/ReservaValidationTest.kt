package com.example.reservaescuela

import com.example.reservaescuela.model.Block
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.isValidTimeRange
import com.example.reservaescuela.model.overlap
import com.example.reservaescuela.model.validateBooking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservaValidationTest {
    private fun lunesFuturo(): LocalDate = LocalDate.now()
        .plusWeeks(3)
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))

    private fun reserva(
        fecha: String = lunesFuturo().toString(),
        inicio: String = "09:00",
        termino: String = "10:00",
        espacio: Int = 1,
        asistentes: Int = 25,
        actividad: String = "Clase de tecnología",
        grupo: String = "7° básico",
        recursos: String = "Proyector",
        taller: Boolean = false
    ) = Booking(
        id = 1L,
        owner = "docente@escuela.cl",
        space = espacio,
        date = fecha,
        start = inicio,
        end = termino,
        activity = actividad,
        group = grupo,
        people = asistentes,
        resources = recursos,
        workshop = taller
    )

    @Test
    fun reservaValidaNoEntregaError() {
        assertNull(validateBooking(reserva(), emptyList(), emptyList()))
    }

    @Test
    fun rechazaReservaSinActividad() {
        assertNotNull(validateBooking(reserva(actividad = ""), emptyList(), emptyList()))
    }

    @Test
    fun rechazaAsistentesSobreLaCapacidad() {
        assertEquals(
            "La cantidad supera la capacidad del espacio.",
            validateBooking(reserva(asistentes = 36), emptyList(), emptyList())
        )
    }

    @Test
    fun rechazaFechaConFormatoIncorrecto() {
        assertEquals(
            "Fecha inválida: usa AAAA-MM-DD.",
            validateBooking(reserva(fecha = "15/12/2027"), emptyList(), emptyList())
        )
    }

    @Test
    fun rechazaReservaEnFinDeSemana() {
        val sabado = lunesFuturo().plusDays(5).toString()
        assertEquals(
            "No se permiten reservas los fines de semana.",
            validateBooking(reserva(fecha = sabado), emptyList(), emptyList())
        )
    }

    @Test
    fun rechazaReservaAntesDeLaApertura() {
        assertEquals(
            "La jornada comienza a las 08:15.",
            validateBooking(reserva(inicio = "08:00"), emptyList(), emptyList())
        )
    }

    @Test
    fun rechazaReservaFueraDeHorarioElViernes() {
        val viernes = lunesFuturo().plusDays(4).toString()
        assertEquals(
            "El viernes termina a las 14:00.",
            validateBooking(reserva(fecha = viernes, inicio = "13:00", termino = "14:30"), emptyList(), emptyList())
        )
    }

    @Test
    fun rechazaReservaCuandoHayBloqueo() {
        val solicitud = reserva()
        val bloqueo = Block(4L, 1, solicitud.date, "09:30", "11:00", "Mantenimiento")
        assertEquals(
            "Horario bloqueado por Inspectoría.",
            validateBooking(solicitud, emptyList(), listOf(bloqueo))
        )
    }

    @Test
    fun rechazaReservaCuandoYaExisteUnaAceptada() {
        val solicitud = reserva()
        val aceptada = solicitud.copy(id = 2L, start = "09:30", end = "10:30", status = "Aceptada")
        assertEquals(
            "El espacio ya está reservado en ese horario.",
            validateBooking(solicitud, listOf(aceptada), emptyList())
        )
    }

    @Test
    fun permiteSolicitudPendienteEnElMismoHorario() {
        val solicitud = reserva()
        val pendiente = solicitud.copy(id = 2L, status = "Pendiente")
        assertNull(validateBooking(solicitud, listOf(pendiente), emptyList()))
    }

    @Test
    fun rechazaHorarioDeInicioPosteriorAlTermino() {
        assertFalse(isValidTimeRange("11:00", "10:00"))
        assertFalse(isValidTimeRange("10:00", "10:00"))
    }

    @Test
    fun aceptaHorarioEnOrdenYSinSolapamientoAlTerminar() {
        assertTrue(isValidTimeRange("09:00", "10:00"))
        assertFalse(overlap("09:00", "10:00", "10:00", "11:00"))
    }
}
