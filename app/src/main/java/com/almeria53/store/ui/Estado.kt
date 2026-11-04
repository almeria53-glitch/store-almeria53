package com.almeria53.store.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.almeria53.store.data.AppEntry
import com.almeria53.store.data.Catalogo
import com.almeria53.store.data.Instalador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Sube cada vez que se vuelve a la tienda, para refrescar "Instalar / Actualizar / Abrir". */
object ResumeBus {
    var tick by mutableIntStateOf(0)
}

/** Todo lo que sabe la tienda: la lista de apps, las descargas en curso y los avisos. */
class TiendaEstado(private val context: Context, private val scope: CoroutineScope) {

    var apps by mutableStateOf<List<AppEntry>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var aviso by mutableStateOf<String?>(null)

    /** Paquete -> avance de descarga (0 a 1). */
    val progreso = mutableStateMapOf<String, Float>()

    private val archivoCache = File(context.filesDir, "catalogo.json")

    init {
        // Primero lo último que se vio (así abre al instante y sin internet), luego se actualiza.
        try {
            if (archivoCache.exists()) apps = Catalogo.parsear(archivoCache.readText())
        } catch (e: Exception) {
        }
        refrescar()
    }

    fun refrescar() {
        if (cargando) return
        scope.launch {
            cargando = true
            error = null
            try {
                val texto = withContext(Dispatchers.IO) { Catalogo.descargarTexto() }
                val nuevas = Catalogo.parsear(texto)
                apps = nuevas
                withContext(Dispatchers.IO) { archivoCache.writeText(texto) }
            } catch (e: Exception) {
                if (apps.isEmpty()) {
                    error = "No se pudo cargar la tienda. Comprueba la conexión a internet."
                } else {
                    aviso = "Sin conexión: se muestra la última lista."
                }
            }
            cargando = false
        }
    }

    /** Lo que hace el botón: abrir si está al día, instalar o actualizar si no. */
    fun pulsar(app: AppEntry) {
        val instalada = Instalador.versionInstalada(context, app.paquete)
        if (instalada != null && instalada >= app.codigo) {
            if (!Instalador.abrir(context, app.paquete)) aviso = "No se puede abrir esta app."
            return
        }
        instalar(app)
    }

    private fun instalar(app: AppEntry) {
        if (progreso.containsKey(app.paquete)) return
        if (app.apk.isBlank()) {
            aviso = "Esta app todavía no tiene archivo para instalar."
            return
        }
        if (!Instalador.puedeInstalar(context)) {
            aviso = "Activa \"Permitir de esta fuente\" y vuelve a pulsar Instalar."
            Instalador.pedirPermiso(context)
            return
        }
        progreso[app.paquete] = 0f
        scope.launch {
            try {
                val archivo = withContext(Dispatchers.IO) {
                    Instalador.descargar(context, app) { p -> progreso[app.paquete] = p }
                }
                progreso.remove(app.paquete)
                Instalador.instalar(context, archivo)
            } catch (e: Exception) {
                progreso.remove(app.paquete)
                aviso = "No se pudo descargar " + app.nombre + "."
            }
        }
    }
}
