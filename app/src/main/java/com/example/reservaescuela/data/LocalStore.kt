package com.example.reservaescuela.data

import android.content.Context
import com.example.reservaescuela.model.Block
import com.example.reservaescuela.model.Notice
import com.example.reservaescuela.model.Ticket
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
            list.add(User(item.getString("email"), item.getString("name"), item.getString("passwordHash"), item.optString("role", "Docente")))
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
                    .put("role", it.role)
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
                    group = item.optString("group", "Sin especificar"),
                    people = item.optInt("people", 1),
                    resources = item.optString("resources", "Ninguno"),
                    workshop = item.optBoolean("workshop"),
                    status = item.optString("status", "Pendiente"),
                    reason = item.optString("reason")
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
                    .put("group", it.group)
                    .put("people", it.people)
                    .put("resources", it.resources)
                    .put("workshop", it.workshop)
                    .put("status", it.status)
                    .put("reason", it.reason)
            )
        }
        writeArray("bookings", array)
    }

    // ---------- bloqueos y avisos ----------
    fun blocks(): List<Block> = readArray("blocks").let { arr ->
        (0 until arr.length()).mapNotNull { index ->
            runCatching { val obj = arr.getJSONObject(index)
                Block(obj.getLong("id"), obj.getInt("space"), obj.getString("date"), obj.getString("start"), obj.getString("end"), obj.getString("reason"))
            }.getOrNull()
        }
    }

    fun saveBlocks(items: List<Block>) {
        val array = JSONArray()
        items.forEach { array.put(JSONObject().put("id", it.id).put("space", it.space).put("date", it.date).put("start", it.start).put("end", it.end).put("reason", it.reason)) }
        writeArray("blocks", array)
    }

    fun notices(): List<Notice> = readArray("notices").let { arr ->
        (0 until arr.length()).mapNotNull { index ->
            runCatching { val obj = arr.getJSONObject(index)
                Notice(obj.getString("email"), obj.getString("message"), obj.getLong("created"))
            }.getOrNull()
        }
    }
    fun saveNotices(items: List<Notice>) {
        val array = JSONArray()
        items.forEach { array.put(JSONObject().put("email", it.email).put("message", it.message).put("created", it.created)) }
        writeArray("notices", array)
    }

    fun tickets(): List<Ticket> = readArray("tickets").let { arr ->
        (0 until arr.length()).mapNotNull { index ->
            runCatching {
                val obj = arr.getJSONObject(index)
                Ticket(obj.getLong("id"), obj.getString("owner"), obj.getInt("space"),
                    obj.getString("location"), obj.getString("severity"), obj.getString("description"),
                    obj.optString("fileUri"), obj.optString("fileName"), obj.optString("fileType"))
            }.getOrNull()
        }
    }
    fun saveTickets(items: List<Ticket>) {
        val array = JSONArray()
        items.forEach {
            array.put(JSONObject().put("id", it.id).put("owner", it.owner).put("space", it.space)
                .put("location", it.location).put("severity", it.severity).put("description", it.description)
                .put("fileUri", it.fileUri).put("fileName", it.fileName).put("fileType", it.fileType))
        }
        writeArray("tickets", array)
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
