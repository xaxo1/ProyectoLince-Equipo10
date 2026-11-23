package cl.duoc.lince.lince;

import cl.duoc.lince.modelo.Enums;
import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.web.ApiExcepcion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

/**
 * Cliente de la Lince API del cliente (SOLO LECTURA). El backend la consulta para volver a
 * verificar que el servicio existe, que está asignado al usuario y cuál es su hotel de pickup.
 */
@Component
public class LinceApiCliente {

    private final RestClient http;

    public LinceApiCliente(RestClient.Builder builder,
                           @Value("${lince.api.url}") String url,
                           @Value("${lince.api.key:}") String apiKey) {
        this.http = builder.baseUrl(url).defaultHeader("X-API-Key", apiKey).build();
    }

    // ----- Partes de la respuesta que usa el backend (los demás campos se ignoran) -----
    public record Recurso(int id, String nombre) {}
    public record Asignacion(String statusPL) {}
    public record Servicio(int id, String codigo, String tipo, String fecha, String horaPickup,
                           String descripcion, String estadoServicio, String pickup, String dropoff,
                           Recurso guia, Recurso transportista, Asignacion asignacion) {}
    public record Meta(int page, int pageSize, int total, int totalPages) {}
    public record Respuesta<T>(T data, Meta meta) {}

    /** GET /api/v1/servicios/{id}. */
    public Servicio servicio(int id) {
        try {
            Respuesta<Servicio> r = http.get().uri("/api/v1/servicios/{id}", id).retrieve()
                    .body(new ParameterizedTypeReference<Respuesta<Servicio>>() {});
            return r.data();
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiExcepcion.noEncontrado("El servicio " + id + " no existe en la Lince API");
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new ApiExcepcion(HttpStatus.BAD_GATEWAY, "LINCE_API_KEY", "La X-API-Key del backend no es válida");
        }
    }

    /** Cuántos servicios tiene asignados el usuario en una fecha (para no bloquear ese día). */
    public int serviciosDelRecursoEn(Usuario usuario, LocalDate fecha) {
        String filtro = usuario.getRol() == Enums.Rol.GUIA ? "guiaId" : "transportistaId";
        Respuesta<Object> r = http.get()
                .uri(u -> u.path("/api/v1/servicios")
                        .queryParam(filtro, usuario.getLinceRecursoId())
                        .queryParam("fechaDesde", fecha)
                        .queryParam("fechaHasta", fecha)
                        .queryParam("pageSize", 1)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<Respuesta<Object>>() {});
        return r.meta() == null ? 0 : r.meta().total();
    }
}
