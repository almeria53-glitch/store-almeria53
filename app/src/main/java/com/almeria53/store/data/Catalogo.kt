package com.almeria53.store.data

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Una app de la tienda, tal como aparece en catalogo.json. */
data class AppEntry(
    val paquete: String,
    val nombre: String,
    val corta: String,
    val descripcion: String,
    val version: String,
    val codigo: Long,
    val apk: String,
    val icono: String,
    val capturas: List<String>,
    val actualizado: String,
    val tamanoMb: Double
)

object Catalogo {
    /** Dónde está la lista de apps. Es un archivo de texto en GitHub. */
    const val CATALOGO_URL =
        "https://raw.githubusercontent.com/almeria53-glitch/catalogo-almeria53/main/catalogo.json"

    fun parsear(texto: String): List<AppEntry> {
        val lista = JSONObject(texto).optJSONArray("apps") ?: return emptyList()
        val resultado = ArrayList<AppEntry>()
        for (i in 0 until lista.length()) {
            val o = lista.optJSONObject(i) ?: continue
            val paquete = o.optString("paquete")
            if (paquete.isBlank()) continue
            val caps = ArrayList<String>()
            val arr = o.optJSONArray("capturas")
            if (arr != null) {
                for (j in 0 until arr.length()) {
                    val u = arr.optString(j)
                    if (u.isNotBlank()) caps.add(u)
                }
            }
            resultado.add(
                AppEntry(
                    paquete = paquete,
                    nombre = o.optString("nombre", paquete),
                    corta = o.optString("corta"),
                    descripcion = o.optString("descripcion"),
                    version = o.optString("version"),
                    codigo = o.optLong("codigo", 1L),
                    apk = o.optString("apk"),
                    icono = o.optString("icono"),
                    capturas = caps,
                    actualizado = o.optString("actualizado"),
                    tamanoMb = o.optDouble("tamanoMb", 0.0)
                )
            )
        }
        return resultado
    }

    /** Descarga el catálogo. Lleva un número al final para saltarse copias viejas. */
    fun descargarTexto(): String {
        val url = URL(CATALOGO_URL + "?t=" + System.currentTimeMillis())
        val c = url.openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 15000
        c.useCaches = false
        try {
            if (c.responseCode !in 200..299) throw IOException("Error " + c.responseCode)
            return c.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            c.disconnect()
        }
    }
}
