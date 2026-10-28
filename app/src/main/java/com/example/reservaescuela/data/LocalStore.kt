package com.example.reservaescuela.data

import android.content.Context
import com.example.reservaescuela.model.Booking
import com.example.reservaescuela.model.Document
import com.example.reservaescuela.model.User
import org.json.JSONArray
import org.json.JSONObject

// guarda los datos en el telefono como texto json
class LocalStore(context: Context) {

    private val prefs = context.getSharedPreferences("escuela_marcela_paz_v3", Context.MODE_PRIVATE)

    // lee un texto guardado y lo convierte en lista json
    private fun readArray(key: String): JSONArray {
        return try {
            JSONArray(prefs.getString(key, "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun writeArray(key: String, array: JSONArray) {
        prefs.edit().putString(key, array.toString()).apply()
    }

    // ---------- usuarios ----------

    fun users(): List<User> {
        val array = readArray("users")
        val list = mutableListOf<User>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(User(item.getString("email"), item.getString("name"), item.getString("passwordHash")))
        }
        return list
    }

    fun saveUsers(users: List<User>) {
        val array = JSONArray()
        users.forEach {
            array.put(
                JSONObject()
                    .put("email", it.email)
                    .put("name", it.name)
                    .put("passwordHash", it.passwordHash)
            )
        }
        writeArray("users", array)
    }

    // ---------- reservas ----------

    fun bookings(): List<Booking> {
        val array = readArray("bookings")
        val list = mutableListOf<Booking>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(
                Booking(
                    id = item.getLong("id"),
                    owner = item.getString("owner"),
                    space = item.getInt("space"),
                    date = item.getString("date"),
                    start = item.getString("start"),
                    end = item.getString("end"),
                    activity = item.getString("activity"),
                    status = item.getString("status")
                )
            )
        }
        return list
    }

    fun saveBookings(bookings: List<Booking>) {
        val array = JSONArray()
        bookings.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("owner", it.owner)
                    .put("space", it.space)
                    .put("date", it.date)
                    .put("start", it.start)
                    .put("end", it.end)
                    .put("activity", it.activity)
                    .put("status", it.status)
            )
        }
        writeArray("bookings", array)
    }

    // ---------- documentos ----------

    fun documents(): List<Document> {
        val array = readArray("documents")
        val list = mutableListOf<Document>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(
                Document(
                    item.getLong("id"),
                    item.getString("owner"),
                    item.getString("name"),
                    item.getString("type"),
                    item.getString("uri")
                )
            )
        }
        return list
    }

    fun saveDocuments(documents: List<Document>) {
        val array = JSONArray()
        documents.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("owner", it.owner)
                    .put("name", it.name)
                    .put("type", it.type)
                    .put("uri", it.uri)
            )
        }
        writeArray("documents", array)
    }

    // ---------- sesion ----------

    fun session(): String = prefs.getString("session", "") ?: ""

    fun saveSession(email: String) {
        prefs.edit().putString("session", email).apply()
    }
}
