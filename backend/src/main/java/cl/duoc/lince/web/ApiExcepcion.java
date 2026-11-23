package cl.duoc.lince.web;

import org.springframework.http.HttpStatus;

/**
 * Error controlado de una regla de negocio: se responde con el código HTTP indicado y un
 * cuerpo {"error": {"code", "message", "requestId"}}, el mismo formato de la Lince API.
 */
public class ApiExcepcion extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;

    public ApiExcepcion(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }

    public HttpStatus getEstado() { return estado; }
    public String getCodigo() { return codigo; }

    public static ApiExcepcion noEncontrado(String mensaje) {
        return new ApiExcepcion(HttpStatus.NOT_FOUND, "NOT_FOUND", mensaje);
    }

    public static ApiExcepcion conflicto(String codigo, String mensaje) {
        return new ApiExcepcion(HttpStatus.CONFLICT, codigo, mensaje);
    }

    public static ApiExcepcion prohibido(String codigo, String mensaje) {
        return new ApiExcepcion(HttpStatus.FORBIDDEN, codigo, mensaje);
    }
}
