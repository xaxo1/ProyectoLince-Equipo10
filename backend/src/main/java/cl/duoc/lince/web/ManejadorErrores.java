package cl.duoc.lince.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Convierte las excepciones en respuestas JSON con requestId (para rastrear el error). */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ApiExcepcion.class)
    public ResponseEntity<Map<String, Object>> regla(ApiExcepcion e) {
        return cuerpo(e.getEstado(), e.getCodigo(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return cuerpo(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) {
        return cuerpo(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> denegado(AuthorizationDeniedException e) {
        return cuerpo(HttpStatus.FORBIDDEN, "FORBIDDEN", "Tu rol no tiene permiso para esta operación");
    }

    public static ResponseEntity<Map<String, Object>> cuerpo(HttpStatus estado, String codigo, String mensaje) {
        Map<String, Object> error = Map.of(
                "code", codigo,
                "message", mensaje == null ? "" : mensaje,
                "requestId", UUID.randomUUID().toString());
        return ResponseEntity.status(estado).body(Map.of("error", error));
    }
}
