package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Confirmacion;
import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.ConfirmacionRepositorio;
import cl.duoc.lince.web.Dtos.ConfirmacionDto;
import cl.duoc.lince.web.Dtos.ConfirmacionSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/confirmaciones")
@Tag(name = "6. Confirmaciones", description = "Confirmar o rechazar una asignación (la Lince API no permite escribir)")
public class ConfirmacionControlador {

    private final ConfirmacionRepositorio confirmaciones;
    private final Clock reloj;

    public ConfirmacionControlador(ConfirmacionRepositorio confirmaciones, Clock reloj) {
        this.confirmaciones = confirmaciones;
        this.reloj = reloj;
    }

    @GetMapping
    @Operation(summary = "Mis confirmaciones y rechazos")
    public List<ConfirmacionDto> mias(@AuthenticationPrincipal Usuario u) {
        return confirmaciones.findByUsuarioId(u.getId()).stream().map(ConfirmacionDto::de).toList();
    }

    @PutMapping("/{servicioId}")
    @Operation(summary = "Confirmar o rechazar un servicio (crea o actualiza)")
    public ConfirmacionDto responder(@AuthenticationPrincipal Usuario u, @PathVariable int servicioId,
                                     @Valid @RequestBody ConfirmacionSolicitud s) {
        Confirmacion c = confirmaciones.findByUsuarioIdAndServicioId(u.getId(), servicioId)
                .orElseGet(() -> new Confirmacion(u, servicioId));
        c.setEstado(s.estado());
        c.setMotivo(s.motivo());
        c.setFechaHora(LocalDateTime.now(reloj));
        return ConfirmacionDto.de(confirmaciones.save(c));
    }
}
