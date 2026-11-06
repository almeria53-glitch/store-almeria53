package com.almeria53.store.data

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Cuenta cuántas veces se pulsa Instalar o Actualizar en cada app.
 * Solo manda el nombre de la app y la versión a GoatCounter. Nada personal.
 * Las cifras se ven únicamente entrando en https://almeria53.goatcounter.com
 */
object Estadisticas {
    private const val CUENTA = "https://almeria53.goatcounter.com/count"

    /** Se llama en segundo plano. Si falla (sin internet, etc.) no pasa nada. */
    fun contarInstalacion(app: AppEntry, esActualizacion: Boolean) {
        try {
            val tipo = if (esActualizacion) "actualizar" else "instalar"
            val ruta = "/" + tipo + "/" + app.paquete
            val titulo = app.nombre + " " + app.version
            val url = URL(
                CUENTA + "?p=" + URLEncoder.encode(ruta, "UTF-8") +
                    "&t=" + URLEncoder.encode(titulo, "UTF-8")
            )
            val c = url.openConnection() as HttpURLConnection
            c.connectTimeout = 8000
            c.readTimeout = 8000
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) StoreAlmeria53")
            try {
                c.responseCode
            } finally {
                c.disconnect()
            }
        } catch (e: Exception) {
        }
    }
}
