package com.almeria53.store.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun TiendaApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val estado = remember { TiendaEstado(context.applicationContext, scope) }
    val prefs = remember { context.getSharedPreferences("store53", Context.MODE_PRIVATE) }
    var tema by remember { mutableStateOf(prefs.getString("tema", "claro") ?: "claro") }

    TiendaTema(modo = tema) {
        val nav = rememberNavController()
        val snack = remember { SnackbarHostState() }

        LaunchedEffect(estado.aviso) {
            val mensaje = estado.aviso
            if (mensaje != null) {
                snack.showSnackbar(mensaje)
                estado.aviso = null
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NavHost(navController = nav, startDestination = "lista") {
                composable("lista") {
                    ListaScreen(
                        estado = estado,
                        tema = tema,
                        onTema = {
                            tema = it
                            prefs.edit().putString("tema", it).apply()
                        },
                        onAbrir = { paquete -> nav.navigate("detalle/$paquete") }
                    )
                }
                composable(
                    route = "detalle/{paquete}",
                    arguments = listOf(navArgument("paquete") { type = NavType.StringType })
                ) { entrada ->
                    DetalleScreen(
                        estado = estado,
                        paquete = entrada.arguments?.getString("paquete") ?: "",
                        onVolver = { nav.popBackStack() }
                    )
                }
            }
            SnackbarHost(hostState = snack, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
