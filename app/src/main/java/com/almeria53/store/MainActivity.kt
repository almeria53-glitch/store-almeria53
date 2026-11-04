package com.almeria53.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.almeria53.store.ui.ResumeBus
import com.almeria53.store.ui.TiendaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TiendaApp() }
    }

    override fun onResume() {
        super.onResume()
        // Al volver de instalar una app, los botones se vuelven a calcular.
        ResumeBus.tick++
    }
}
