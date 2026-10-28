package com.example.reservaescuela.utils

import java.security.MessageDigest

// guarda la clave como hash para no dejarla en texto plano
fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
