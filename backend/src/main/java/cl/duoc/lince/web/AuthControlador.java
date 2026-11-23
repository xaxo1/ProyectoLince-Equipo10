package cl.duoc.lince.web;

import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.UsuarioRepositorio;
import cl.duoc.lince.seguridad.JwtServicio;
import cl.duoc.lince.web.Dtos.LoginRespuesta;
import cl.duoc.lince.web.Dtos.LoginSolicitud;
import cl.duoc.lince.web.Dtos.UsuarioDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticación", description = "Login con RUT o correo; entrega un JWT con el rol")
public class AuthControlador {

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder encoder;
    private final JwtServicio jwt;

    public AuthControlador(UsuarioRepositorio usuarios, PasswordEncoder encoder, JwtServicio jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión (guía, transportista o coordinación)")
    public LoginRespuesta login(@Valid @RequestBody LoginSolicitud s) {
        String id = s.identificador().trim();
        Usuario u = (id.contains("@") ? usuarios.findByCorreoIgnoreCase(id) : usuarios.findByRut(normalizarRut(id)))
                .filter(x -> encoder.matches(s.clave(), x.getClaveHash()))
                .orElseThrow(() -> new ApiExcepcion(HttpStatus.UNAUTHORIZED, "CREDENCIALES", "RUT/correo o contraseña incorrectos"));
        return new LoginRespuesta(jwt.generar(u), UsuarioDto.de(u));
    }

    @GetMapping("/yo")
    @Operation(summary = "Datos del usuario del token")
    public UsuarioDto yo(@AuthenticationPrincipal Usuario u) {
        return UsuarioDto.de(u);
    }

    /** "15.482.910-k" -> "15482910K" (los RUT se guardan sin puntos ni guion). */
    public static String normalizarRut(String rut) {
        return rut.replaceAll("[^0-9kK]", "").toUpperCase();
    }
}
