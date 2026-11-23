package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Enums;
import cl.duoc.lince.modelo.Incidencia;
import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.IncidenciaRepositorio;
import cl.duoc.lince.web.Dtos.IncidenciaDto;
import cl.duoc.lince.web.Dtos.IncidenciaSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
@Tag(name = "3. Incidencias", description = "CRUD de incidencias (coordinación ve todas)")
public class IncidenciaControlador {

    private final IncidenciaRepositorio incidencias;
    private final Clock reloj;

    public IncidenciaControlador(IncidenciaRepositorio incidencias, Clock reloj) {
        this.incidencias = incidencias;
        this.reloj = reloj;
    }

    @GetMapping
    @Operation(summary = "Listar incidencias (las mías; coordinación ve todas)")
    public List<IncidenciaDto> listar(@AuthenticationPrincipal Usuario u) {
        List<Incidencia> lista = u.getRol() == Enums.Rol.COORDINADOR
                ? incidencias.findAllByOrderByFechaHoraDesc()
                : incidencias.findByUsuarioIdOrderByFechaHoraDesc(u.getId());
        return lista.stream().map(IncidenciaDto::de).toList();
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Incidencias de un servicio")
    public List<IncidenciaDto> delServicio(@PathVariable int servicioId) {
        return incidencias.findByServicioIdOrderByFechaHoraDesc(servicioId).stream().map(IncidenciaDto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear incidencia")
    public IncidenciaDto crear(@AuthenticationPrincipal Usuario u, @Valid @RequestBody IncidenciaSolicitud s) {
        Incidencia i = new Incidencia(u, s.servicioId(), s.tipo(), s.descripcion().trim(), s.pasajerosPresentes(),
                false, LocalDateTime.now(reloj));
        return IncidenciaDto.de(incidencias.save(i));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar incidencia (o cerrarla con estado CERRADA)")
    public IncidenciaDto editar(@AuthenticationPrincipal Usuario u, @PathVariable long id,
                                @Valid @RequestBody IncidenciaSolicitud s) {
        Incidencia i = propia(u, id);
        i.setTipo(s.tipo());
        i.setDescripcion(s.descripcion().trim());
        i.setPasajerosPresentes(s.pasajerosPresentes());
        if (s.estado() != null) i.setEstado(s.estado());
        return IncidenciaDto.de(incidencias.save(i));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar incidencia")
    public void eliminar(@AuthenticationPrincipal Usuario u, @PathVariable long id) {
        incidencias.delete(propia(u, id));
    }

    /** El dueño puede editar su incidencia; coordinación puede editar cualquiera. */
    private Incidencia propia(Usuario u, long id) {
        Incidencia i = incidencias.findById(id)
                .orElseThrow(() -> ApiExcepcion.noEncontrado("No existe la incidencia " + id));
        if (u.getRol() != Enums.Rol.COORDINADOR && !i.getUsuario().getId().equals(u.getId())) {
            throw ApiExcepcion.prohibido("NO_ES_TUYA", "Esa incidencia es de otro usuario");
        }
        return i;
    }
}
