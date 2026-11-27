package com.example.reservaescuela

import com.example.reservaescuela.data.*
import com.example.reservaescuela.ui.MaterialesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MaterialesViewModelTest {
    private val cola = StandardTestDispatcher()
    private var filas = emptyList<Material>()
    private val inventario = object : MaterialesApi {
        override suspend fun listar() = filas
        override suspend fun agregar(entrada: Entrada) {
            filas += Material(7, entrada.nombre, entrada.categoria)
        }
        override suspend fun editar(id: Long, entrada: Entrada) {
            filas = filas.map { if (it.id == id) it.copy(nombre = entrada.nombre) else it }
        }
        override suspend fun eliminar(id: Long) { filas = filas.filterNot { it.id == id } }
    }
    private val catalogo = object : CatalogoApi { override suspend fun listar() = listOf("Lectura") }
    private val feriados = object : FeriadosApi {
        override suspend fun listar(anio: Int) = listOf(Feriado("$anio-12-31", "Fin de año"))
    }
    private lateinit var vm: MaterialesViewModel

    @Before fun preparar() {
        Dispatchers.setMain(cola)
        vm = MaterialesViewModel(catalogo, inventario, feriados)
        cola.scheduler.advanceUntilIdle()
    }
    @After fun terminar() = Dispatchers.resetMain()

    @Test fun cargaCategoriasYFeriado() {
        assertEquals(listOf("Lectura"), vm.categorias)
        assertTrue(vm.feriado.contains("Fin de año"))
    }
    @Test fun actualizaDatosDeOtroCliente() {
        filas = listOf(Material(3, "Atlas", "Lectura"))
        vm.recargar()
        cola.scheduler.advanceUntilIdle()
        assertEquals(filas, vm.materiales)
    }
    @Test fun agregaEditaYEliminaDesdeElFormulario() {
        vm.guardar(null, Entrada("Atlas", "Lectura")) {}
        cola.scheduler.advanceUntilIdle()
        assertEquals("Atlas", vm.materiales.single().nombre)
        vm.guardar(7, Entrada("Mapas", "Lectura")) {}
        cola.scheduler.advanceUntilIdle()
        assertEquals("Mapas", vm.materiales.single().nombre)
        vm.eliminar(7)
        cola.scheduler.advanceUntilIdle()
        assertTrue(vm.materiales.isEmpty())
        assertFalse(vm.ocupado)
    }
}
