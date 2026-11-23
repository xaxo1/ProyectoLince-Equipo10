package com.equipo10.lince.model

import java.util.Locale

/** Coordenadas GPS del dispositivo y su precisión (radio de error, en metros). */
data class Ubicacion(val latitud: Double, val longitud: Double, val precisionMetros: Double = 0.0) {

    /** "-33.43722, -70.65041" (Locale.US: punto decimal, como en Google Maps) */
    fun texto(): String = String.format(Locale.US, "%.5f, %.5f", latitud, longitud)
}
