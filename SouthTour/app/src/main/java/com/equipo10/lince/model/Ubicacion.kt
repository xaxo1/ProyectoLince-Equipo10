package com.equipo10.lince.model

import java.util.Locale

/** Coordenadas GPS obtenidas del dispositivo al hacer check-in / check-out. */
data class Ubicacion(val latitud: Double, val longitud: Double) {

    /** "-33.43722, -70.65041" (Locale.US: punto decimal, como en Google Maps) */
    fun texto(): String = String.format(Locale.US, "%.5f, %.5f", latitud, longitud)
}
