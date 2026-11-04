package com.almeria53.store.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.almeria53.store.data.AppEntry
import com.almeria53.store.data.Instalador

@Composable
fun ListaScreen(
    estado: TiendaEstado,
    tema: String,
    onTema: (String) -> Unit,
    onAbrir: (String) -> Unit
) {
    val context = LocalContext.current
    var busqueda by remember { mutableStateOf("") }
    var menuTema by remember { mutableStateOf(false) }
    val tick = ResumeBus.tick

    val visibles = estado.apps.filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
    val porActualizar = remember(tick, estado.apps) {
        estado.apps.count {
            val v = Instalador.versionInstalada(context, it.paquete)
            v != null && v < it.codigo
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Store Almería 53", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { estado.refrescar() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar lista")
                    }
                    Box {
                        IconButton(onClick = { menuTema = true }) {
                            Icon(Icons.Filled.Palette, contentDescription = "Tema")
                        }
                        DropdownMenu(expanded = menuTema, onDismissRequest = { menuTema = false }) {
                            DropdownMenuItem(
                                text = { Text((if (tema == "claro") "✓ " else "    ") + "Claro") },
                                onClick = { onTema("claro"); menuTema = false }
                            )
                            DropdownMenuItem(
                                text = { Text((if (tema == "oscuro") "✓ " else "    ") + "Oscuro") },
                                onClick = { onTema("oscuro"); menuTema = false }
                            )
                            DropdownMenuItem(
                                text = { Text((if (tema == "auto") "✓ " else "    ") + "Automático (según el móvil)") },
                                onClick = { onTema("auto"); menuTema = false }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar apps") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (estado.apps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = estado.error
                                ?: if (estado.cargando) "Cargando la tienda…" else "Todavía no hay apps en la tienda.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (estado.error != null) {
                            Spacer(modifier = Modifier.padding(8.dp))
                            Button(onClick = { estado.refrescar() }, shape = RoundedCornerShape(50)) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (porActualizar > 0) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (porActualizar == 1) "1 app tiene una actualización disponible"
                                    else "$porActualizar apps tienen una actualización disponible",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    items(visibles, key = { it.paquete }) { app ->
                        FilaApp(app = app, estado = estado, onAbrir = onAbrir)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaApp(app: AppEntry, estado: TiendaEstado, onAbrir: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrir(app.paquete) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoApp(app = app, tamano = 56.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = app.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (app.corta.isNotBlank()) {
                Text(
                    text = app.corta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "Versión " + app.version + (if (app.tamanoMb > 0) "  ·  " + app.tamanoMb + " MB" else ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        BotonApp(app = app, estado = estado, grande = false)
    }
}
