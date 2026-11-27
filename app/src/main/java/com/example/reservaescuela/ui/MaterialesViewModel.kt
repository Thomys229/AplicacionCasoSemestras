package com.example.reservaescuela.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaescuela.data.*
import java.time.LocalDate
import java.io.IOException
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MaterialesViewModel(
    private val catalogo: CatalogoApi = RetrofitService.catalogo,
    private val inventario: MaterialesApi = RetrofitService.materiales,
    private val calendario: FeriadosApi = RetrofitService.feriados
) : ViewModel() {
    var materiales by mutableStateOf(emptyList<Material>())
        private set
    var categorias by mutableStateOf(emptyList<String>())
        private set
    var ocupado by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf("")
        private set
    var feriado by mutableStateOf("")
        private set

    init {
        recargar()
        viewModelScope.launch {
            val hoy = LocalDate.now()
            feriado = try {
                val siguiente = calendario.listar(hoy.year)
                    .firstOrNull { it.fecha >= hoy.toString() }
                siguiente?.let { "${it.fecha} · ${it.nombre}" } ?: "Sin feriados pendientes este año."
            } catch (e: IOException) {
                "Sin conexión con Nager.Date."
            } catch (e: HttpException) {
                "Consulta de feriados no disponible."
            }
        }
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        if (ocupado) return
        ocupado = true
        mensaje = ""
        viewModelScope.launch {
            try {
                accion()
            } catch (e: IOException) {
                mensaje = "Revisa los servicios y la conexión USB. Actualiza la lista antes de reintentar."
            } catch (e: HttpException) {
                mensaje = "El servidor respondió ${e.code()}. Actualiza la lista."
            } finally {
                ocupado = false
            }
        }
    }

    fun recargar() = ejecutar {
        materiales = inventario.listar()
        categorias = catalogo.listar()
    }

    fun guardar(id: Long?, entrada: Entrada, guardado: () -> Unit) = ejecutar {
        if (id == null) inventario.agregar(entrada)
        else inventario.editar(id, entrada)
        guardado()
        materiales = inventario.listar()
    }

    fun eliminar(id: Long) = ejecutar {
        inventario.eliminar(id)
        materiales = inventario.listar()
    }
}
