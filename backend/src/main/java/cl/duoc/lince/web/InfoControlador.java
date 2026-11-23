package cl.duoc.lince.web;

import cl.duoc.lince.servicio.Reglas;
import cl.duoc.lince.web.Dtos.ReglasDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "0. Info", description = "Estado del servicio y reglas vigentes")
public class InfoControlador {

    private final Reglas reglas;

    public InfoControlador(Reglas reglas) {
        this.reglas = reglas;
    }

    @GetMapping("/health")
    @Operation(summary = "¿Está vivo el backend?")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/reglas")
    @Operation(summary = "Reglas vigentes: radio, precisión GPS, minutos de anticipación y días para bloquear")
    public ReglasDto reglas() {
        return new ReglasDto(reglas.radioDefecto(), reglas.precisionMaxima(), reglas.minutosAntes(),
                reglas.diasAnticipacion(), reglas.zona().getId());
    }
}
