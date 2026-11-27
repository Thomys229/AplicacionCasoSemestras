package com.example.reservaescuela.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaescuela.data.*
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import org.json.JSONObject

class AgendaViewModel : ViewModel() {
    private var api: AgendaApi? = null
    private val acceso = Mutex()
    var usuario by mutableStateOf("")
        private set
    var turnos by mutableStateOf(emptyList<TurnoNube>())
        private set
    var mensaje by mutableStateOf("")
        private set
    var estadoConexion by mutableStateOf("")
        private set
    var enviando by mutableStateOf(false)
        private set

    private suspend fun intentar(accion: suspend () -> Unit) {
        try {
            accion()
            mensaje = ""
        } catch (e: IOException) {
            mensaje = "Sin conexión. Comprueba internet o espera que Render inicie."
        } catch (e: HttpException) {
            val detalle = e.response()?.errorBody()?.string().orEmpty()
            mensaje = runCatching { JSONObject(detalle).getString("mensaje") }.getOrNull()
                ?: when (e.code()) {
                    401, 403 -> "Revisa el usuario, la contraseña y los permisos."
                    else -> "El servidor respondió ${e.code()}."
                }
        }
    }

    fun conectar(url: String, nombre: String, clave: String) {
        if (enviando) return
        val servicio = try { agendaApi(url, nombre, clave) } catch (e: IllegalArgumentException) {
            mensaje = "Escribe la URL HTTPS de tu servicio, sin rutas."
            return
        }
        enviando = true
        viewModelScope.launch {
            try {
                intentar {
                    turnos = servicio.listar()
                    api = servicio
                    usuario = nombre
                }
            } finally { enviando = false }
        }
    }

    suspend fun actualizar() = acceso.withLock {
        val servicio = api ?: return@withLock
        try {
            turnos = servicio.listar()
            estadoConexion = ""
        } catch (e: IOException) {
            estadoConexion = "Sin conexión. Mostrando la última consulta."
        } catch (e: HttpException) {
            estadoConexion = "No se pudo actualizar la agenda (${e.code()})."
        }
    }

    fun guardar(solicitud: SolicitudNube, listo: () -> Unit) = cambiar { servicio ->
        servicio.crear(solicitud)
        listo()
    }

    fun decidir(id: String, estado: String) = cambiar { it.decidir(id, DecisionNube(estado)) }

    private fun cambiar(accion: suspend (AgendaApi) -> Unit) {
        val servicio = api ?: return
        if (enviando) return
        enviando = true
        viewModelScope.launch {
            try {
                acceso.withLock { intentar { accion(servicio); turnos = servicio.listar() } }
            } finally { enviando = false }
        }
    }
}
