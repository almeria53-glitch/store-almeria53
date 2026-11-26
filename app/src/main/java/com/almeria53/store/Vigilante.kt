package com.almeria53.store

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.almeria53.store.data.Catalogo
import com.almeria53.store.data.Instalador

/** Mira cada 12 horas si hay actualizaciones y, si las hay, avisa con una notificación. */
class Vigilante : JobService() {

    companion object {
        private const val ID_TRABAJO = 5301
        private const val ID_AVISO = 5302
        private const val CANAL = "actualizaciones"
        private const val DOCE_HORAS = 12L * 60L * 60L * 1000L

        fun programar(context: Context) {
            try {
                val js = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                if (js.getPendingJob(ID_TRABAJO) != null) return
                val info = JobInfo.Builder(ID_TRABAJO, ComponentName(context, Vigilante::class.java))
                    .setPeriodic(DOCE_HORAS)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build()
                js.schedule(info)
            } catch (e: Exception) {
            }
        }
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Thread {
            try {
                revisar()
            } catch (e: Exception) {
            }
            jobFinished(params, false)
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false

    private fun revisar() {
        val ctx = applicationContext
        val apps = Catalogo.parsear(Catalogo.descargarTexto())
        val pendientes = apps.filter {
            val v = Instalador.versionInstalada(ctx, it.paquete)
            v != null && v < it.codigo
        }
        val propia = Instalador.versionInstalada(ctx, ctx.packageName) ?: 0L
        val tienda = Catalogo.leerVersionTienda()?.takeIf { it.codigo > propia }

        val firma = pendientes.joinToString(",") { it.paquete + ":" + it.codigo } +
            (if (tienda != null) "|t" + tienda.codigo else "")
        val prefs = ctx.getSharedPreferences("store53", Context.MODE_PRIVATE)
        val nm = NotificationManagerCompat.from(ctx)

        if (firma.isEmpty()) {
            nm.cancel(ID_AVISO)
            prefs.edit().remove("ultimoAviso").apply()
            return
        }
        // Si ya se avisó de exactamente lo mismo, no se vuelve a avisar.
        if (prefs.getString("ultimoAviso", "") == firma) return
        if (!nm.areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= 26) {
            val canal = NotificationChannel(CANAL, "Actualizaciones", NotificationManager.IMPORTANCE_DEFAULT)
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(canal)
        }

        val total = pendientes.size + (if (tienda != null) 1 else 0)
        val nombres = pendientes.map { it.nombre } + (if (tienda != null) listOf("la propia tienda") else emptyList())
        val titulo = if (total == 1) "1 actualización disponible" else "$total actualizaciones disponibles"
        val texto = nombres.joinToString(", ")

        val intento = Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("actualizaciones", true)
        val pi = PendingIntent.getActivity(ctx, 0, intento, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val n = NotificationCompat.Builder(ctx, CANAL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setNumber(total)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(ID_AVISO, n)
        } catch (e: SecurityException) {
            return
        }
        prefs.edit().putString("ultimoAviso", firma).apply()
    }
}
