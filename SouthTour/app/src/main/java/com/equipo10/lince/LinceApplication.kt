package com.equipo10.lince

import android.app.Application
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.data.UbicacionDispositivo
import com.equipo10.lince.sync.SincronizarHitosWorker

/**
 * Se crea ANTES que cualquier pantalla: arma el repositorio (Retrofit + Room + DataStore),
 * prepara el GPS y, si quedaron hitos sin enviar, programa su sincronización.
 */
class LinceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.inicializar(this)
        UbicacionDispositivo.inicializar(this)
        SincronizarHitosWorker.programar(this)
    }
}
