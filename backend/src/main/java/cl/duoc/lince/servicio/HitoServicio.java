package cl.duoc.lince.servicio;

import cl.duoc.lince.lince.LinceApiCliente;
import cl.duoc.lince.modelo.*;
import cl.duoc.lince.modelo.Enums.ResultadoHito;
import cl.duoc.lince.modelo.Enums.TipoHito;
import cl.duoc.lince.repositorio.GeocercaRepositorio;
import cl.duoc.lince.repositorio.HitoRepositorio;
import cl.duoc.lince.repositorio.IncidenciaRepositorio;
import cl.duoc.lince.web.ApiExcepcion;
import cl.duoc.lince.web.Dtos.HitoSolicitud;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Optional;
import java.util.Set;

/**
 * Registra check-in, check-out y posiciones. Antes de guardar, el servidor VUELVE a verificar:
 * que el servicio exista y esté asignado al usuario, que no esté cancelado, la distancia a la
 * geocerca, la precisión del GPS y la regla de los 15 minutos. Si el check-in falla, crea una incidencia.
 */
@Service
public class HitoServicio {

    private static final Set<String> ASIGNACION_ANULADA = Set.of("CX", "DL");

    private final HitoRepositorio hitos;
    private final GeocercaRepositorio geocercas;
    private final IncidenciaRepositorio incidencias;
    private final LinceApiCliente lince;
    private final Reglas reglas;
    private final Clock reloj;

    public HitoServicio(HitoRepositorio hitos, GeocercaRepositorio geocercas, IncidenciaRepositorio incidencias,
                        LinceApiCliente lince, Reglas reglas, Clock reloj) {
        this.hitos = hitos;
        this.geocercas = geocercas;
        this.incidencias = incidencias;
        this.lince = lince;
        this.reglas = reglas;
        this.reloj = reloj;
    }

    @Transactional
    public Hito registrar(Usuario usuario, HitoSolicitud s) {
        // 1) Idempotencia: si el teléfono reenvía el mismo hito (offline), se devuelve el ya guardado
        Optional<Hito> repetido = hitos.findByIdempotencia(s.idempotencia());
        if (repetido.isPresent()) return repetido.get();

        // 2) El servicio debe existir en la Lince API y estar asignado a este usuario
        LinceApiCliente.Servicio servicio = lince.servicio(s.servicioId());
        validarAsignacion(usuario, servicio);

        LocalDateTime ahora = LocalDateTime.now(reloj);
        Hito hito = new Hito(s.idempotencia(), usuario, s.servicioId(), s.tipo(),
                s.latitud(), s.longitud(), s.precisionMetros(), s.fechaHoraDispositivo());
        hito.setFechaHoraServidor(ahora);

        switch (s.tipo()) {
            case CHECK_IN -> validarCheckIn(usuario, servicio, hito, ahora);
            case CHECK_OUT -> registrarCheckOut(usuario, servicio, hito);
            case POSICION -> registrarPosicion(usuario, servicio, hito);
        }
        Hito guardado = hitos.save(hito);

        // 3) Check-in rechazado -> incidencia automática para coordinación
        if (guardado.getResultado() != ResultadoHito.VERIFICADA && guardado.getResultado() != ResultadoHito.REGISTRADA) {
            String tipo = guardado.getResultado() == ResultadoHito.ATRASADA ? "ATRASO" : guardado.getResultado().name();
            incidencias.save(new Incidencia(usuario, s.servicioId(), tipo, guardado.getMensaje(), null, true, ahora));
        }
        return guardado;
    }

    private void validarAsignacion(Usuario usuario, LinceApiCliente.Servicio servicio) {
        Integer recurso = switch (usuario.getRol()) {
            case GUIA -> servicio.guia() == null ? null : servicio.guia().id();
            case TRANSPORTISTA -> servicio.transportista() == null ? null : servicio.transportista().id();
            case COORDINADOR -> throw ApiExcepcion.prohibido("ROL_INVALIDO", "Coordinación no registra hitos");
        };
        if (recurso == null || !recurso.equals(usuario.getLinceRecursoId())) {
            throw ApiExcepcion.prohibido("NO_ASIGNADO", "El servicio " + servicio.id() + " no está asignado a " + usuario.getNombre());
        }
        String statusPL = servicio.asignacion() == null ? null : servicio.asignacion().statusPL();
        if ("XX".equals(servicio.estadoServicio()) || ASIGNACION_ANULADA.contains(statusPL)) {
            throw ApiExcepcion.conflicto("SERVICIO_CANCELADO", "El servicio está cancelado (estado "
                    + servicio.estadoServicio() + ", asignación " + statusPL + ")");
        }
    }

    private void validarCheckIn(Usuario usuario, LinceApiCliente.Servicio servicio, Hito hito, LocalDateTime ahora) {
        if (hitos.findByServicioIdAndUsuarioIdOrderByFechaHoraServidorAsc(servicio.id(), usuario.getId()).stream()
                .anyMatch(h -> h.getTipo() == TipoHito.CHECK_IN && h.getResultado() == ResultadoHito.VERIFICADA)) {
            throw ApiExcepcion.conflicto("YA_REGISTRADO", "La llegada a este servicio ya fue verificada");
        }
        LocalDate fecha = LocalDate.parse(servicio.fecha());
        if (!fecha.equals(ahora.toLocalDate())) {
            throw ApiExcepcion.conflicto("NO_ES_HOY", "Solo se puede hacer check-in el día del servicio (" + fecha + ")");
        }
        hito.setLugar(servicio.pickup());
        Optional<Geocerca> geocerca = geocercas.findByNombreIgnoreCase(servicio.pickup());
        if (geocerca.isEmpty()) {
            hito.setResultado(ResultadoHito.FUERA_DE_ZONA);
            hito.setMensaje("El pickup \"" + servicio.pickup() + "\" no tiene geocerca: no se puede verificar la llegada");
            return;
        }
        Geocerca g = geocerca.get();
        double distancia = ReglasGeocerca.distanciaMetros(hito.getLatitud(), hito.getLongitud(), g.getLatitud(), g.getLongitud());
        hito.setDistanciaMetros(Math.round(distancia * 10) / 10.0);
        hito.setRadioMetros(g.getRadioMetros());

        LocalDateTime pickup = LocalDateTime.of(fecha, LocalTime.parse(servicio.horaPickup()));
        ResultadoHito resultado = ReglasGeocerca.evaluarCheckIn(distancia, g.getRadioMetros(), hito.getPrecisionMetros(),
                reglas.precisionMaxima(), ahora, pickup, reglas.minutosAntes());
        hito.setResultado(resultado);
        hito.setMensaje(switch (resultado) {
            case VERIFICADA -> "Llegada verificada a " + Math.round(distancia) + " m de " + g.getNombre();
            case FUERA_DE_ZONA -> "Fuera de zona: a " + Math.round(distancia) + " m (radio " + g.getRadioMetros() + " m)";
            case GPS_IMPRECISO -> "GPS impreciso: ±" + Math.round(hito.getPrecisionMetros()) + " m (máximo " + Math.round(reglas.precisionMaxima()) + " m)";
            case ATRASADA -> "Llegada tardía: debía llegar " + reglas.minutosAntes() + " min antes del pickup de las " + servicio.horaPickup();
            case REGISTRADA -> "";
        });
    }

    private void registrarCheckOut(Usuario usuario, LinceApiCliente.Servicio servicio, Hito hito) {
        if (!hitos.existsByServicioIdAndUsuarioIdAndTipo(servicio.id(), usuario.getId(), TipoHito.CHECK_IN)) {
            throw ApiExcepcion.conflicto("SIN_CHECK_IN", "Primero debes registrar el check-in");
        }
        if (hitos.existsByServicioIdAndUsuarioIdAndTipo(servicio.id(), usuario.getId(), TipoHito.CHECK_OUT)) {
            throw ApiExcepcion.conflicto("YA_REGISTRADO", "El check-out de este servicio ya fue registrado");
        }
        hito.setLugar(servicio.dropoff());
        geocercas.findByNombreIgnoreCase(servicio.dropoff()).ifPresent(g -> {
            double d = ReglasGeocerca.distanciaMetros(hito.getLatitud(), hito.getLongitud(), g.getLatitud(), g.getLongitud());
            hito.setDistanciaMetros(Math.round(d * 10) / 10.0);
            hito.setRadioMetros(g.getRadioMetros());
        });
        hito.setResultado(ResultadoHito.REGISTRADA);
        hito.setMensaje("Check-out en " + servicio.dropoff());
    }

    private void registrarPosicion(Usuario usuario, LinceApiCliente.Servicio servicio, Hito hito) {
        if (usuario.getRol() != Enums.Rol.TRANSPORTISTA) {
            throw ApiExcepcion.prohibido("ROL_INVALIDO", "Solo el transportista reporta su posición");
        }
        boolean activo = hitos.existsByServicioIdAndUsuarioIdAndTipo(servicio.id(), usuario.getId(), TipoHito.CHECK_IN)
                && !hitos.existsByServicioIdAndUsuarioIdAndTipo(servicio.id(), usuario.getId(), TipoHito.CHECK_OUT);
        if (!activo) {
            throw ApiExcepcion.conflicto("SERVICIO_NO_ACTIVO", "Solo se reporta posición durante un servicio en curso (sin rastreo 24/7)");
        }
        hito.setResultado(ResultadoHito.REGISTRADA);
        hito.setMensaje("Posición reportada");
    }
}
