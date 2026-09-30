package com.example.datossinmvvm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenProducto() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.productoDao()
    val scope = rememberCoroutineScope()

    var listaProductos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var productoEditando by remember { mutableStateOf<Producto?>(null) }

    fun cargarProductos() {
        scope.launch {
            listaProductos = dao.getAll()
        }
    }

    LaunchedEffect(Unit) {
        cargarProductos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CRUD Proyecto: Productos") },
                actions = {
                    IconButton(onClick = { cargarProductos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = if (productoEditando == null) "Registrar Producto" else "Editar Producto",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (S/)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val pPrecio = precio.toDoubleOrNull() ?: 0.0
                    val pStock = stock.toIntOrNull() ?: 0

                    if (nombre.isNotBlank()) {
                        scope.launch {
                            if (productoEditando == null) {
                                dao.insert(Producto(nombre = nombre, precio = pPrecio, stock = pStock))
                            } else {
                                dao.update(
                                    Producto(
                                        id = productoEditando!!.id,
                                        nombre = nombre,
                                        precio = pPrecio,
                                        stock = pStock
                                    )
                                )
                                productoEditando = null
                            }
                            nombre = ""
                            precio = ""
                            stock = ""
                            cargarProductos()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (productoEditando == null) "Guardar Producto" else "Actualizar Producto")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lista de Registros (${listaProductos.size}):",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaProductos) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = prod.nombre, style = MaterialTheme.typography.titleSmall)
                                Text(text = "Precio: S/ ${prod.precio} | Stock: ${prod.stock} unids.")
                            }

                            Row {
                                IconButton(onClick = {
                                    productoEditando = prod
                                    nombre = prod.nombre
                                    precio = prod.precio.toString()
                                    stock = prod.stock.toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }

                                IconButton(onClick = {
                                    scope.launch {
                                        dao.delete(prod)
                                        cargarProductos()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}