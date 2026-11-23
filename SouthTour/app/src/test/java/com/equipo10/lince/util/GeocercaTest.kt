package com.equipo10.lince.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la geocerca: distancia (Haversine) y la regla de llegada.
 * Casos límite: justo en el radio, un metro fuera, justo 15 min antes y GPS impreciso.
 */
class GeocercaTest {

    private val hotelLat = -33.41450
    private val hotelLon = -70.59400

    @Test
    fun `la distancia a uno mismo es cero`() {
        assertEquals(0.0, Geocerca.distanciaMetros(hotelLat, hotelLon, hotelLat, hotelLon), 0.001)
    }

    @Test
    fun `0,001 grados de latitud son unos 111 metros`() {
        val d = Geocerca.distanciaMetros(hotelLat, hotelLon, hotelLat + 0.001, hotelLon)
        assertTrue("distancia = $d", d in 110.0..112.5)
    }

    @Test
    fun `dentro del radio, a tiempo y buena precision es VERIFICADA`() {
        val r = Geocerca.evaluar(distanciaMetros = 40.0, radioMetros = 150, precisionMetros = 10.0,
            precisionMaxima = 50.0, minutosQueFaltan = 60, minutosAntes = 15)
        assertEquals(ResultadoLlegada.VERIFICADA, r)
    }

    @Test
    fun `justo en el borde del radio todavia es valida`() {
        assertEquals(ResultadoLlegada.VERIFICADA, Geocerca.evaluar(150.0, 150, 10.0, 50.0, 30, 15))
    }

    @Test
    fun `un metro fuera del radio es FUERA_DE_ZONA`() {
        assertEquals(ResultadoLlegada.FUERA_DE_ZONA, Geocerca.evaluar(151.0, 150, 10.0, 50.0, 30, 15))
    }

    @Test
    fun `exactamente 15 minutos antes es puntual y 14 es atraso`() {
        assertEquals(ResultadoLlegada.VERIFICADA, Geocerca.evaluar(10.0, 150, 5.0, 50.0, 15, 15))
        assertEquals(ResultadoLlegada.ATRASADA, Geocerca.evaluar(10.0, 150, 5.0, 50.0, 14, 15))
    }

    @Test
    fun `despues de la hora de pickup es ATRASADA`() {
        assertEquals(ResultadoLlegada.ATRASADA, Geocerca.evaluar(10.0, 150, 5.0, 50.0, -5, 15))
    }

    @Test
    fun `GPS impreciso se revisa antes que la distancia`() {
        assertEquals(ResultadoLlegada.GPS_IMPRECISO, Geocerca.evaluar(500.0, 150, 80.0, 50.0, 60, 15))
    }

    @Test
    fun `con radio de 100 metros, 120 metros queda fuera`() {
        assertEquals(ResultadoLlegada.FUERA_DE_ZONA, Geocerca.evaluar(120.0, 100, 10.0, 50.0, 60, 15))
    }
}
