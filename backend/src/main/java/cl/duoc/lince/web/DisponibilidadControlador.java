package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.BloqueoRepositorio;
import cl.duoc.lince.servicio.DisponibilidadServicio;
import cl.duoc.lince.web.Dtos.BloqueoDto;
import cl.duoc.lince.web.Dtos.BloqueoSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
@Tag(name = "5. Disponibilidad", description = "Bloqueo de días (7 días de anticipación, nunca sobre un servicio)")
public class DisponibilidadControlador {

    private final DisponibilidadServicio servicio;
    private final BloqueoRepositorio bloqueos;

    public DisponibilidadControlador(DisponibilidadServicio servicio, BloqueoRepositorio bloqueos) {
        this.servicio = servicio;
        this.bloqueos = bloqueos;
    }

    @GetMapping
    @Operation(summary = "Mis días bloqueados")
    public List<BloqueoDto> mios(@AuthenticationPrincipal Usuario u) {
        return bloqueos.findByUsuarioIdOrderByFechaAsc(u.getId()).stream().map(BloqueoDto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bloquear un día")
    public BloqueoDto bloquear(@AuthenticationPrincipal Usuario u, @Valid @RequestBody BloqueoSolicitud s) {
        return BloqueoDto.de(servicio.bloquear(u, s.fecha(), s.motivo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desbloquear un día")
    public void desbloquear(@AuthenticationPrincipal Usuario u, @PathVariable long id) {
        servicio.desbloquear(u, id);
    }
}
