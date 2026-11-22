package com.example.reservaescuela

import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.validateBooking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import org.junit.Assert.assertEquals
import org.junit.Test

class ReservaValidationTest {

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

        val resultado = validateBooking(reserva, emptyList(), emptyList())

        assertEquals("La cantidad supera la capacidad del espacio.", resultado)
    }
}
