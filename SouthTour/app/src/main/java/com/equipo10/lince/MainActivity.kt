package com.equipo10.lince

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.data.SesionDataStore
import com.equipo10.lince.ui.navigation.LinceApp
import com.equipo10.lince.ui.theme.LinceTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var idioma: String = "es"

    /** Aplica el idioma guardado en DataStore ANTES de crear la pantalla (español / inglés). */
    override fun attachBaseContext(base: Context) {
        idioma = SesionDataStore.idiomaGuardado(base)
        val configuracion = Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(idioma)) }
        super.attachBaseContext(base.createConfigurationContext(configuracion))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            LinceTheme {
                LinceApp(idiomaActual = idioma, onCambiarIdioma = ::cambiarIdioma)
            }
        }
    }

    /** Guarda el idioma (persistente) y recrea la Activity para que todos los textos cambien. */
    private fun cambiarIdioma(codigo: String) {
        if (codigo == idioma) return
        lifecycleScope.launch {
            ServiceLocator.sesion.guardarIdioma(codigo)
            recreate()
        }
    }
}
