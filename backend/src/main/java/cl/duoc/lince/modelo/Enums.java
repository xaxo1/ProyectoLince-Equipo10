package cl.duoc.lince.modelo;

/** Enumeraciones del dominio, agrupadas en un solo archivo para leerlas juntas. */
public final class Enums {
    private Enums() {}

    /** Roles de la app: el pasajero no inicia sesión. */
    public enum Rol { GUIA, TRANSPORTISTA, COORDINADOR }

    /** Tipo de hito que registra el teléfono. POSICION = reporte puntual del transportista en un servicio activo. */
    public enum TipoHito { CHECK_IN, CHECK_OUT, POSICION }

    /** Resultado de validar un hito en el servidor. */
    public enum ResultadoHito {
        VERIFICADA,      // dentro del radio, a tiempo y con buena precisión
        FUERA_DE_ZONA,   // más lejos que el radio de la geocerca
        ATRASADA,        // a menos de 15 min del pickup (o después)
        GPS_IMPRECISO,   // precisión peor que la máxima aceptada
        REGISTRADA       // check-out o posición: se registra sin rechazar
    }

    public enum EstadoConfirmacion { CONFIRMADA, RECHAZADA }

    public enum EstadoIncidencia { ABIERTA, CERRADA }
}
