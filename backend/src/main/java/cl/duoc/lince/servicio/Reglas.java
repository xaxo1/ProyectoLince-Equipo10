package cl.duoc.lince.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZoneId;

/** Valores de las reglas del negocio, leídos desde application.properties (lince.*). */
@Component
public class Reglas {

    private final int radioDefecto;
    private final double precisionMaxima;
    private final int minutosAntes;
    private final int diasAnticipacion;
    private final ZoneId zona;

    public Reglas(@Value("${lince.geocerca.radio-defecto}") int radioDefecto,
                  @Value("${lince.geocerca.precision-maxima}") double precisionMaxima,
                  @Value("${lince.puntualidad.minutos-antes}") int minutosAntes,
                  @Value("${lince.disponibilidad.dias-anticipacion}") int diasAnticipacion,
                  @Value("${lince.zona-horaria}") String zona) {
        this.radioDefecto = radioDefecto;
        this.precisionMaxima = precisionMaxima;
        this.minutosAntes = minutosAntes;
        this.diasAnticipacion = diasAnticipacion;
        this.zona = ZoneId.of(zona);
    }

    public int radioDefecto() { return radioDefecto; }
    public double precisionMaxima() { return precisionMaxima; }
    public int minutosAntes() { return minutosAntes; }
    public int diasAnticipacion() { return diasAnticipacion; }
    public ZoneId zona() { return zona; }

    /** Reloj del servidor en hora de Chile: la hora del SERVIDOR es la evidencia de cada hito. */
    @Bean
    public Clock reloj() {
        return Clock.system(zona);
    }
}
