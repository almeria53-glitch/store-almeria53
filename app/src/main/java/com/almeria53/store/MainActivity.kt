package com.almeria53.store

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.almeria53.store.ui.ResumeBus
import com.almeria53.store.ui.TiendaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.getBooleanExtra("actualizaciones", false) == true) {
            ResumeBus.abrirActualizaciones = true
        }
        pedirNotificaciones()
        Vigilante.programar(this)
        setContent { TiendaApp() }
    }

    /** En Android 13 o más nuevo hay que pedir permiso para avisar de actualizaciones (una sola vez). */
    private fun pedirNotificaciones() {
        if (Build.VERSION.SDK_INT < 33) return
        val prefs = getSharedPreferences("store53", MODE_PRIVATE)
        if (prefs.getBoolean("pidioNotif", false)) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        prefs.edit().putBoolean("pidioNotif", true).apply()
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    }

    override fun onResume() {
        super.onResume()
        // Al volver de instalar una app, los botones se vuelven a calcular.
        ResumeBus.tick++
    }
}
