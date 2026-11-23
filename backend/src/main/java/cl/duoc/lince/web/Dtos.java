package cl.duoc.lince.web;

import cl.duoc.lince.modelo.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTOs: lo que entra y sale por la API en JSON. Las validaciones (@NotBlank, @Size...)
 * se revisan automáticamente con @Valid y devuelven 400 si fallan.
 */
public final class Dtos {
    private Dtos() {}

    // ---------- Autenticación ----------
    public record LoginSolicitud(@NotBlank String identificador, @NotBlank String clave) {}

    public record UsuarioDto(Long id, String nombre, String correo, String rut, Enums.Rol rol, Integer linceRecursoId) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNombre(), u.getCorreo(), u.getRut(), u.getRol(), u.getLinceRecursoId());
        }
    }

    public record LoginRespuesta(String token, UsuarioDto usuario) {}

    // ---------- Hitos (check-in / check-out / posición) ----------
    public record HitoSolicitud(
            @NotBlank String idempotencia,
            @NotNull Integer servicioId,
            @NotNull Enums.TipoHito tipo,
            @DecimalMin("-90") @DecimalMax("90") double latitud,
            @DecimalMin("-180") @DecimalMax("180") double longitud,
            @PositiveOrZero double precisionMetros,
            String fechaHoraDispositivo) {}

    public record HitoDto(Long id, String idempotencia, int servicioId, Enums.TipoHito tipo,
                          double latitud, double longitud, double precisionMetros,
                          Double distanciaMetros, Integer radioMetros, String lugar,
                          Enums.ResultadoHito resultado, String mensaje, String fechaHoraServidor,
                          Long usuarioId, String usuarioNombre) {
        public static HitoDto de(Hito h) {
            return new HitoDto(h.getId(), h.getIdempotencia(), h.getServicioId(), h.getTipo(),
                    h.getLatitud(), h.getLongitud(), h.getPrecisionMetros(), h.getDistanciaMetros(),
                    h.getRadioMetros(), h.getLugar(), h.getResultado(), h.getMensaje(),
                    String.valueOf(h.getFechaHoraServidor()), h.getUsuario().getId(), h.getUsuario().getNombre());
        }
    }

    // ---------- Incidencias ----------
    public record IncidenciaSolicitud(
            @NotNull Integer servicioId,
            @NotBlank String tipo,
            @NotBlank @Size(min = 10, max = 500) String descripcion,
            @PositiveOrZero Integer pasajerosPresentes,
            Enums.EstadoIncidencia estado) {}

    public record IncidenciaDto(Long id, int servicioId, String tipo, String descripcion, Integer pasajerosPresentes,
                                boolean automatica, Enums.EstadoIncidencia estado, String fechaHora,
                                Long usuarioId, String usuarioNombre) {
        public static IncidenciaDto de(Incidencia i) {
            return new IncidenciaDto(i.getId(), i.getServicioId(), i.getTipo(), i.getDescripcion(),
                    i.getPasajerosPresentes(), i.isAutomatica(), i.getEstado(), String.valueOf(i.getFechaHora()),
                    i.getUsuario().getId(), i.getUsuario().getNombre());
        }
    }

    // ---------- Geocercas ----------
    public record GeocercaSolicitud(
            @NotBlank String nombre,
            @NotBlank String oficina,
            @DecimalMin("-90") @DecimalMax("90") double latitud,
            @DecimalMin("-180") @DecimalMax("180") double longitud,
            @Min(10) @Max(5000) int radioMetros) {}

    public record GeocercaDto(Long id, String nombre, String oficina, double latitud, double longitud, int radioMetros) {
        public static GeocercaDto de(Geocerca g) {
            return new GeocercaDto(g.getId(), g.getNombre(), g.getOficina(), g.getLatitud(), g.getLongitud(), g.getRadioMetros());
        }
    }

    // ---------- Disponibilidad ----------
    public record BloqueoSolicitud(@NotNull LocalDate fecha, @Size(max = 200) String motivo) {}

    public record BloqueoDto(Long id, String fecha, String motivo) {
        public static BloqueoDto de(Bloqueo b) {
            return new BloqueoDto(b.getId(), b.getFecha().toString(), b.getMotivo());
        }
    }

    // ---------- Confirmaciones ----------
    public record ConfirmacionSolicitud(@NotNull Enums.EstadoConfirmacion estado, @Size(max = 200) String motivo) {}

    public record ConfirmacionDto(int servicioId, Enums.EstadoConfirmacion estado, String motivo, String fechaHora) {
        public static ConfirmacionDto de(Confirmacion c) {
            return new ConfirmacionDto(c.getServicioId(), c.getEstado(), c.getMotivo(), String.valueOf(c.getFechaHora()));
        }
    }

    // ---------- Reglas vigentes (la app las muestra en pantalla) ----------
    public record ReglasDto(int radioDefecto, double precisionMaxima, int minutosAntes, int diasAnticipacion, String zonaHoraria) {}
}
