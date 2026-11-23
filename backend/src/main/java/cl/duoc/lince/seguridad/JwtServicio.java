package cl.duoc.lince.seguridad;

import cl.duoc.lince.modelo.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/** Crea y valida los tokens JWT. El token guarda el id del usuario (subject) y su rol. */
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final Duration validez;

    public JwtServicio(@Value("${lince.jwt.secreto:}") String secreto,
                       @Value("${lince.jwt.horas-validez:12}") long horas) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // Sin secreto configurado: uno aleatorio (los tokens dejan de servir al reiniciar)
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.validez = Duration.ofHours(horas);
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(validez)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el id del usuario si el token es válido; lanza excepción si no lo es. */
    public long idUsuario(String token) {
        String subject = Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token).getPayload().getSubject();
        return Long.parseLong(subject);
    }
}
