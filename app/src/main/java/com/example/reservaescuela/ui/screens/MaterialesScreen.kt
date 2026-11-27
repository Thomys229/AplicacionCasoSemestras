package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaescuela.data.Entrada
import com.example.reservaescuela.ui.MaterialesViewModel

@Composable
fun MaterialesScreen(vm: MaterialesViewModel = viewModel()) {
    var id by rememberSaveable { mutableStateOf<Long?>(null) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    fun limpiar() { id = null; nombre = ""; categoria = "" }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Próximo feriado de Chile", style = MaterialTheme.typography.titleMedium)
            Text(vm.feriado.ifEmpty { "Consultando…" })
        }
        item {
            OutlinedTextField(nombre, { nombre = it.take(80) }, enabled = !vm.ocupado,
                label = { Text("Nombre del material") }, singleLine = true)
            Text("Categoría")
            vm.categorias.forEach { opcion ->
                FilterChip(selected = categoria == opcion, onClick = { categoria = opcion },
                    enabled = !vm.ocupado, label = { Text(opcion) })
            }
            Row {
                Button(onClick = { vm.guardar(id, Entrada(nombre.trim(), categoria), ::limpiar) },
                    enabled = !vm.ocupado && nombre.isNotBlank() && categoria in vm.categorias) {
                    Text(if (id == null) "Agregar" else "Guardar cambios")
                }
                if (id != null) TextButton(onClick = ::limpiar, enabled = !vm.ocupado) { Text("Cancelar") }
            }
        }
        item {
            OutlinedButton(onClick = vm::recargar, enabled = !vm.ocupado) { Text("Actualizar lista") }
            if (vm.mensaje.isNotEmpty()) Text(vm.mensaje)
        }
        items(vm.materiales, key = { it.id }) { material ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${material.nombre} · ${material.categoria}")
                    Row {
                        TextButton(enabled = !vm.ocupado, onClick = {
                            id = material.id; nombre = material.nombre; categoria = material.categoria
                        }) { Text("Editar") }
                        TextButton(enabled = !vm.ocupado, onClick = {
                            if (id == material.id) limpiar()
                            vm.eliminar(material.id)
                        }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}
