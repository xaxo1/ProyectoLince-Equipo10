package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Geocerca;
import cl.duoc.lince.repositorio.GeocercaRepositorio;
import cl.duoc.lince.web.Dtos.GeocercaDto;
import cl.duoc.lince.web.Dtos.GeocercaSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geocercas")
@Tag(name = "4. Geocercas", description = "Hoteles y puntos de pickup con su radio (crear/editar: solo coordinación)")
public class GeocercaControlador {

    private final GeocercaRepositorio geocercas;

    public GeocercaControlador(GeocercaRepositorio geocercas) {
        this.geocercas = geocercas;
    }

    @GetMapping
    @Operation(summary = "Listar geocercas")
    public List<GeocercaDto> listar() {
        return geocercas.findAll().stream().map(GeocercaDto::de).toList();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Geocerca de un lugar por su nombre (ej: Hotel Las Condes)")
    public GeocercaDto porNombre(@RequestParam String nombre) {
        return GeocercaDto.de(geocercas.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> ApiExcepcion.noEncontrado("No hay geocerca para " + nombre)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear geocerca")
    public GeocercaDto crear(@Valid @RequestBody GeocercaSolicitud s) {
        if (geocercas.findByNombreIgnoreCase(s.nombre()).isPresent()) {
            throw ApiExcepcion.conflicto("DUPLICADA", "Ya existe una geocerca para " + s.nombre());
        }
        return GeocercaDto.de(geocercas.save(
                new Geocerca(s.nombre(), s.oficina(), s.latitud(), s.longitud(), s.radioMetros())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar geocerca (coordenada o radio)")
    public GeocercaDto editar(@PathVariable long id, @Valid @RequestBody GeocercaSolicitud s) {
        Geocerca g = geocercas.findById(id)
                .orElseThrow(() -> ApiExcepcion.noEncontrado("No existe la geocerca " + id));
        g.setNombre(s.nombre());
        g.setOficina(s.oficina());
        g.setLatitud(s.latitud());
        g.setLongitud(s.longitud());
        g.setRadioMetros(s.radioMetros());
        return GeocercaDto.de(geocercas.save(g));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar geocerca")
    public void eliminar(@PathVariable long id) {
        geocercas.deleteById(id);
    }
}
