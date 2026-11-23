package cl.duoc.lince.servicio;

import cl.duoc.lince.modelo.Enums.ResultadoHito;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Reglas puras (sin base de datos) para validar una llegada. Son las mismas que calcula el
 * teléfono: el servidor las vuelve a aplicar porque el teléfono no es de confianza.
 */
public final class ReglasGeocerca {

    private static final double RADIO_TIERRA_METROS = 6_371_000.0;

    private ReglasGeocerca() {}

    /** Distancia en metros entre dos coordenadas (fórmula de Haversine). */
    public static double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_METROS * Math.asin(Math.sqrt(a));
    }

    /**
     * Resultado de un check-in. El orden importa: primero la precisión (si el GPS es malo, la
     * distancia no es confiable), después la distancia y al final el horario.
     */
    public static ResultadoHito evaluarCheckIn(double distanciaMetros, int radioMetros,
                                               double precisionMetros, double precisionMaxima,
                                               LocalDateTime ahora, LocalDateTime pickup, int minutosAntes) {
        if (precisionMetros > precisionMaxima) return ResultadoHito.GPS_IMPRECISO;
        if (distanciaMetros > radioMetros) return ResultadoHito.FUERA_DE_ZONA;
        long minutosQueFaltan = Duration.between(ahora, pickup).toMinutes();
        if (minutosQueFaltan < minutosAntes) return ResultadoHito.ATRASADA;
        return ResultadoHito.VERIFICADA;
    }
}
