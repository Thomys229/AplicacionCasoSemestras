package com.example.reservaescuela

import com.example.reservaescuela.utils.hashPassword
import org.junit.Assert.assertEquals
import org.junit.Test

class SeguridadTest {

    @Test
    fun generaHashDeLaContrasena() {
        assertEquals(
            "8d969eef6ecad3c29a3a629280e686cff8fab4d" +
                "a6d9c3af9e9eebfa1b6a9a5a5",
            hashPassword("123456")
        )
    }
}
