package com.example.reservaescuela

import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.isValidTimeRange
import com.example.reservaescuela.model.nextBusinessDay
import com.example.reservaescuela.model.overlap
import com.example.reservaescuela.model.spaceName
import com.example.reservaescuela.model.validateBooking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservaValidationTest {

    @Test
    fun obtieneNombreDelEspacio() {
        assertEquals("Laboratorio de computación", spaceName(1))
    }

    @Test
    fun obtieneSiguienteDiaHabil() {
        val fecha = nextBusinessDay()
        assertTrue(fecha.isAfter(LocalDate.now()))
        assertFalse(fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY)
    }

    @Test
    fun detectaHorariosQueSeCruzan() {
        assertTrue(overlap("09:00", "10:00", "09:30", "11:00"))
    }

    @Test
    fun rechazaHoraDeInicioPosteriorAlTermino() {
        assertFalse(isValidTimeRange("11:00", "10:00"))
    }

    @Test
    fun rechazaReservaSiSuperaLaCapacidad() {
        val fecha = LocalDate.now()
            .plusWeeks(3)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            .toString()

        val reserva = Booking(
            id = 1L,
            owner = "docente@escuela.cl",
            space = 1,
            date = fecha,
            start = "09:00",
            end = "10:00",
            activity = "Clase de tecnología",
            group = "7° básico",
            people = 36,
            resources = "Proyector"
        )

        assertEquals(
            "La cantidad supera la capacidad del espacio.",
            validateBooking(reserva, emptyList(), emptyList())
        )
    }
}
