package com.almeria53.store.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Claro = lightColorScheme(
    primary = Color(0xFF01875F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F0E3),
    onPrimaryContainer = Color(0xFF00291C),
    secondary = Color(0xFF4F6358),
    background = Color.White,
    onBackground = Color(0xFF191C1A),
    surface = Color.White,
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFEFF2F0),
    onSurfaceVariant = Color(0xFF5B615D),
    outline = Color(0xFFD0D5D2)
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFF5BD6A5),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFFB5F2D6),
    secondary = Color(0xFFB4CCBE),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE0E3E0),
    surface = Color(0xFF101412),
    onSurface = Color(0xFFE0E3E0),
    surfaceVariant = Color(0xFF2A302D),
    onSurfaceVariant = Color(0xFFBFC9C3),
    outline = Color(0xFF404944)
)

/** modo: "claro" (por defecto), "oscuro" o "auto" (según el móvil). */
@Composable
fun TiendaTema(modo: String, content: @Composable () -> Unit) {
    val oscuro = when (modo) {
        "oscuro" -> true
        "auto" -> isSystemInDarkTheme()
        else -> false
    }
    val colores = if (oscuro) Oscuro else Claro
    val vista = LocalView.current
    SideEffect {
        val ventana = (vista.context as? Activity)?.window
        if (ventana != null) {
            ventana.statusBarColor = colores.surface.toArgb()
            ventana.navigationBarColor = colores.surface.toArgb()
            val control = WindowCompat.getInsetsController(ventana, vista)
            control.isAppearanceLightStatusBars = !oscuro
            control.isAppearanceLightNavigationBars = !oscuro
        }
    }
    MaterialTheme(colorScheme = colores, content = content)
}
