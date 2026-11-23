package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.HitoRepositorio;
import cl.duoc.lince.servicio.HitoServicio;
import cl.duoc.lince.web.Dtos.HitoDto;
import cl.duoc.lince.web.Dtos.HitoSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hitos")
@Tag(name = "2. Hitos", description = "Check-in / check-out con geocerca y posiciones puntuales")
public class HitoControlador {

    private final HitoServicio servicio;
    private final HitoRepositorio hitos;

    public HitoControlador(HitoServicio servicio, HitoRepositorio hitos) {
        this.servicio = servicio;
        this.hitos = hitos;
    }

    @PostMapping
    @Operation(summary = "Registrar un hito (el servidor vuelve a validar geocerca, horario y asignación)")
    public HitoDto registrar(@AuthenticationPrincipal Usuario u, @Valid @RequestBody HitoSolicitud s) {
        return HitoDto.de(servicio.registrar(u, s));
    }

    @GetMapping("/mios")
    @Operation(summary = "Mis hitos, del más reciente al más antiguo")
    public List<HitoDto> mios(@AuthenticationPrincipal Usuario u) {
        return hitos.findByUsuarioIdOrderByFechaHoraServidorDesc(u.getId()).stream().map(HitoDto::de).toList();
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Mis hitos de un servicio")
    public List<HitoDto> delServicio(@AuthenticationPrincipal Usuario u, @PathVariable int servicioId) {
        return hitos.findByServicioIdAndUsuarioIdOrderByFechaHoraServidorAsc(servicioId, u.getId())
                .stream().map(HitoDto::de).toList();
    }
}
