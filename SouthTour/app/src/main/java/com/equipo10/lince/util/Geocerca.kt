package com.equipo10.lince.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Resultado de validar la llegada (mismos nombres que usa el backend). */
enum class ResultadoLlegada { VERIFICADA, FUERA_DE_ZONA, ATRASADA, GPS_IMPRECISO }

/**
 * Reglas de la geocerca, calculadas en el TELÉFONO para mostrarlas antes de enviar.
 * El backend vuelve a calcular lo mismo: la decisión final siempre es la del servidor.
 */
object Geocerca {

    private const val RADIO_TIERRA_METROS = 6_371_000.0

    /** Distancia en metros entre dos coordenadas (fórmula de Haversine). */
    fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * RADIO_TIERRA_METROS * asin(sqrt(a))
    }

    /**
     * Orden de las reglas: precisión del GPS -> distancia al hotel -> horario.
     * [minutosQueFaltan] = minutos entre ahora y la hora de pickup (negativo si ya pasó).
     */
    fun evaluar(
        distanciaMetros: Double,
        radioMetros: Int,
        precisionMetros: Double,
        precisionMaxima: Double,
        minutosQueFaltan: Long,
        minutosAntes: Int
    ): ResultadoLlegada = when {
        precisionMetros > precisionMaxima -> ResultadoLlegada.GPS_IMPRECISO
        distanciaMetros > radioMetros -> ResultadoLlegada.FUERA_DE_ZONA
        minutosQueFaltan < minutosAntes -> ResultadoLlegada.ATRASADA
        else -> ResultadoLlegada.VERIFICADA
    }
}
