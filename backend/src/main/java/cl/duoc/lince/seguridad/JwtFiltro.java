package cl.duoc.lince.seguridad;

import cl.duoc.lince.repositorio.UsuarioRepositorio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header "Authorization: Bearer <token>" de cada petición. Si el token es válido,
 * deja al Usuario como "principal" y su rol como ROLE_GUIA / ROLE_TRANSPORTISTA / ROLE_COORDINADOR.
 */
@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtServicio jwt;
    private final UsuarioRepositorio usuarios;

    public JwtFiltro(JwtServicio jwt, UsuarioRepositorio usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                long id = jwt.idUsuario(header.substring(7));
                usuarios.findById(id).ifPresent(usuario -> {
                    var rol = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
                    var auth = new UsernamePasswordAuthenticationToken(usuario, null, List.of(rol));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (RuntimeException tokenInvalido) {
                // Token vencido o adulterado: la petición sigue sin usuario y Spring responde 401
            }
        }
        chain.doFilter(request, response);
    }
}
