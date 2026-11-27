package com.example.reservaescuela.model

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingDatesTest {
    private val friday = LocalDate.of(2027, 1, 1)

    @Test fun reservaDelViernesParteElLunes() {
        for (offset in -1L..2L) {
            assertNotNull(bookingDateError(friday.plusDays(offset), friday))
        }
        assertNull(bookingDateError(friday.plusDays(3), friday))
    }

    @Test fun permiteElSiguienteDiaHabilYFechasLejanas() {
        val monday = friday.plusDays(3)
        assertNull(bookingDateError(monday.plusDays(1), monday))
        assertNull(bookingDateError(monday.plusWeeks(150), monday))
        assertNotNull(bookingDateError(monday.plusDays(5), monday))
    }

    @Test fun inspectoriaPuedeBloquearDesdeHoyEnDiasDeSemana() {
        assertFalse(canBlockDate(friday.minusDays(1), friday))
        assertTrue(canBlockDate(friday, friday))
        assertFalse(canBlockDate(friday.plusDays(1), friday))
        assertFalse(canBlockDate(friday.plusDays(2), friday))
        assertTrue(canBlockDate(friday.plusDays(3), friday))
    }
}
