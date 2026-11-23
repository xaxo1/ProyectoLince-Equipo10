package cl.duoc.lince.servicio;

import cl.duoc.lince.lince.LinceApiCliente;
import cl.duoc.lince.modelo.Bloqueo;
import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.BloqueoRepositorio;
import cl.duoc.lince.web.ApiExcepcion;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Regla: un día se bloquea con al menos 7 días de anticipación y nunca sobre un servicio asignado. */
@Service
public class DisponibilidadServicio {

    private final BloqueoRepositorio bloqueos;
    private final LinceApiCliente lince;
    private final Reglas reglas;
    private final Clock reloj;

    public DisponibilidadServicio(BloqueoRepositorio bloqueos, LinceApiCliente lince, Reglas reglas, Clock reloj) {
        this.bloqueos = bloqueos;
        this.lince = lince;
        this.reglas = reglas;
        this.reloj = reloj;
    }

    public Bloqueo bloquear(Usuario usuario, LocalDate fecha, String motivo) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(reloj), fecha);
        if (dias < reglas.diasAnticipacion()) {
            throw ApiExcepcion.conflicto("SIN_ANTICIPACION",
                    "Debes bloquear con al menos " + reglas.diasAnticipacion() + " días de anticipación (faltan " + dias + ")");
        }
        if (bloqueos.findByUsuarioIdAndFecha(usuario.getId(), fecha).isPresent()) {
            throw ApiExcepcion.conflicto("YA_BLOQUEADO", "Ese día ya estaba bloqueado");
        }
        if (usuario.getLinceRecursoId() != null && lince.serviciosDelRecursoEn(usuario, fecha) > 0) {
            throw ApiExcepcion.conflicto("TIENE_SERVICIO", "No se puede bloquear: ese día tienes un servicio asignado");
        }
        return bloqueos.save(new Bloqueo(usuario, fecha, motivo));
    }

    public void desbloquear(Usuario usuario, long idBloqueo) {
        Bloqueo b = bloqueos.findById(idBloqueo)
                .orElseThrow(() -> ApiExcepcion.noEncontrado("No existe el bloqueo " + idBloqueo));
        if (!b.getUsuario().getId().equals(usuario.getId())) {
            throw ApiExcepcion.prohibido("NO_ES_TUYO", "Ese bloqueo es de otro usuario");
        }
        bloqueos.delete(b);
    }
}
