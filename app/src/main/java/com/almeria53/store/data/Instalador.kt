package com.almeria53.store.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object Instalador {

    /** Versión (número interno) de la app si ya está instalada; si no, null. */
    fun versionInstalada(context: Context, paquete: String): Long? {
        return try {
            PackageInfoCompat.getLongVersionCode(context.packageManager.getPackageInfo(paquete, 0))
        } catch (e: Exception) {
            null
        }
    }

    fun abrir(context: Context, paquete: String): Boolean {
        val intento = context.packageManager.getLaunchIntentForPackage(paquete) ?: return false
        intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intento)
        return true
    }

    /** True si el móvil ya deja a esta tienda instalar apps. */
    fun puedeInstalar(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 26) context.packageManager.canRequestPackageInstalls() else true
    }

    /** Lleva a la pantalla de Ajustes donde se permite instalar desde esta tienda. */
    fun pedirPermiso(context: Context) {
        val intento = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:" + context.packageName)
        )
        intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intento)
    }

    /** Descarga el APK a la carpeta temporal y avisa del avance (0 a 1). */
    fun descargar(context: Context, app: AppEntry, alAvanzar: (Float) -> Unit): File {
        val carpeta = File(context.cacheDir, "apks")
        carpeta.mkdirs()
        val destino = File(carpeta, app.paquete + ".apk")
        val parcial = File(carpeta, app.paquete + ".part")

        val c = URL(app.apk).openConnection() as HttpURLConnection
        c.connectTimeout = 20000
        c.readTimeout = 30000
        try {
            c.connect()
            if (c.responseCode !in 200..299) throw IOException("Error " + c.responseCode)
            val total = c.contentLengthLong
            c.inputStream.use { entrada ->
                parcial.outputStream().use { salida ->
                    val buffer = ByteArray(32 * 1024)
                    var leidos = 0L
                    while (true) {
                        val n = entrada.read(buffer)
                        if (n < 0) break
                        salida.write(buffer, 0, n)
                        leidos += n
                        if (total > 0) alAvanzar(leidos.toFloat() / total.toFloat())
                    }
                }
            }
        } finally {
            c.disconnect()
        }
        if (destino.exists()) destino.delete()
        if (!parcial.renameTo(destino)) {
            parcial.copyTo(destino, overwrite = true)
            parcial.delete()
        }
        return destino
    }

    /** Abre el instalador de Android con el APK descargado. */
    fun instalar(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", archivo)
        val intento = Intent(Intent.ACTION_VIEW)
        intento.setDataAndType(uri, "application/vnd.android.package-archive")
        intento.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intento)
    }
}
