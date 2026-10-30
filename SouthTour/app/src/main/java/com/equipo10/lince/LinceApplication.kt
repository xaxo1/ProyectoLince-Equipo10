package com.equipo10.lince

import android.app.Application
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.UbicacionDispositivo

/**
 * Se crea ANTES que cualquier pantalla: entrega el Context a la base de datos Room
 * y al GPS (se registra en el AndroidManifest con android:name).
 */
class LinceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RepositorioLince.inicializar(this)
        UbicacionDispositivo.inicializar(this)
    }
}
