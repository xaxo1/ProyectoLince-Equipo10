package cl.duoc.lince.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad: sin sesiones (cada petición trae su JWT). Rutas públicas: login, Swagger y health.
 * Coordinación es la única que puede crear/editar geocercas y ver el panel operativo.
 */
@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtFiltro jwtFiltro) throws Exception {
        http
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error", "/api/auth/login", "/api/health", "/api/reglas",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/coordinacion/**").hasRole("COORDINADOR")
                        .requestMatchers(HttpMethod.POST, "/api/geocercas/**").hasRole("COORDINADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/geocercas/**").hasRole("COORDINADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/geocercas/**").hasRole("COORDINADOR")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribir(res, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Falta el token o está vencido. Inicia sesión."))
                        .accessDeniedHandler((req, res, ex) ->
                                escribir(res, HttpStatus.FORBIDDEN, "FORBIDDEN", "Tu rol no tiene permiso para esta operación")))
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Respuesta JSON de error para 401/403 (mismo formato que ManejadorErrores). */
    private static void escribir(jakarta.servlet.http.HttpServletResponse res, HttpStatus estado,
                                 String codigo, String mensaje) throws java.io.IOException {
        res.setStatus(estado.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\":{\"code\":\"" + codigo + "\",\"message\":\"" + mensaje
                + "\",\"requestId\":\"" + java.util.UUID.randomUUID() + "\"}}");
    }
}
