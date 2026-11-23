package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Enums;
import cl.duoc.lince.repositorio.HitoRepositorio;
import cl.duoc.lince.repositorio.UsuarioRepositorio;
import cl.duoc.lince.web.Dtos.HitoDto;
import cl.duoc.lince.web.Dtos.UsuarioDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coordinacion")
@Tag(name = "7. Coordinación", description = "Panel operativo (solo rol COORDINADOR)")
public class CoordinacionControlador {

    private final HitoRepositorio hitos;
    private final UsuarioRepositorio usuarios;
    private final Clock reloj;

    public CoordinacionControlador(HitoRepositorio hitos, UsuarioRepositorio usuarios, Clock reloj) {
        this.hitos = hitos;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @GetMapping("/hitos")
    @Operation(summary = "Hitos de un día (por defecto hoy): verificados, atrasados, fuera de zona...")
    public List<HitoDto> hitosDelDia(@RequestParam(required = false) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        return hitos.findByFechaHoraServidorBetweenOrderByFechaHoraServidorDesc(
                        dia.atStartOfDay(), dia.plusDays(1).atStartOfDay())
                .stream().map(HitoDto::de).toList();
    }

    @GetMapping("/posiciones")
    @Operation(summary = "Última posición reportada por cada transportista (puntos puntuales, sin rastreo 24/7)")
    public List<HitoDto> ultimasPosiciones() {
        return usuarios.findAll().stream()
                .filter(u -> u.getRol() == Enums.Rol.TRANSPORTISTA)
                .flatMap(u -> hitos.findByUsuarioIdOrderByFechaHoraServidorDesc(u.getId()).stream().limit(1))
                .map(HitoDto::de)
                .toList();
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Usuarios académicos y su recurso en la Lince API")
    public List<UsuarioDto> usuarios() {
        return usuarios.findAll().stream().map(UsuarioDto::de).toList();
    }
}
