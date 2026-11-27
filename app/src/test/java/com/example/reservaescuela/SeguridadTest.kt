package com.example.reservaescuela

import com.example.reservaescuela.utils.hashPassword
import org.junit.Assert.assertEquals
import org.junit.Test

class SeguridadTest {

    @Test
    fun generaHashDeLaContrasena() {
        assertEquals(
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a" +
                "86aff3ca12020c923adc6c92",
            hashPassword("123456")
        )
    }
}
