package com.almeria53.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.almeria53.store.data.AppEntry
import com.almeria53.store.data.Instalador

/** Icono de la app. Si no hay imagen (o no carga), se ve su inicial sobre fondo verde claro. */
@Composable
fun IconoApp(app: AppEntry, tamano: Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano * 0.22f))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = app.nombre.take(1).uppercase(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = (tamano.value * 0.42f).sp
        )
        if (app.icono.isNotBlank()) {
            AsyncImage(
                model = app.icono,
                contentDescription = app.nombre,
                modifier = Modifier.size(tamano),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/** Botón que cambia solo: Instalar, Actualizar, Abrir o el avance de la descarga. */
@Composable
fun BotonApp(app: AppEntry, estado: TiendaEstado, grande: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tick = ResumeBus.tick
    val instalada = remember(tick, app.paquete, app.codigo) {
        Instalador.versionInstalada(context, app.paquete)
    }
    val avance = estado.progreso[app.paquete]
    val forma = RoundedCornerShape(50)

    if (avance != null) {
        if (grande) {
            Box(modifier = modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { avance },
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Descargando " + (avance * 100).toInt() + " %")
                }
            }
        } else {
            Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { avance }, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        }
        return
    }

    val etiqueta = when {
        instalada == null -> "Instalar"
        instalada < app.codigo -> "Actualizar"
        else -> "Abrir"
    }
    val ancho = if (grande) modifier.fillMaxWidth().height(48.dp) else modifier
    if (etiqueta == "Abrir") {
        OutlinedButton(onClick = { estado.pulsar(app) }, modifier = ancho, shape = forma) { Text(etiqueta) }
    } else {
        Button(onClick = { estado.pulsar(app) }, modifier = ancho, shape = forma) { Text(etiqueta) }
    }
}
