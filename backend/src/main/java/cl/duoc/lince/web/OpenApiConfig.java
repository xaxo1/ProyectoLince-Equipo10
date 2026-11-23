package cl.duoc.lince.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI con el botón "Authorize" para pegar el JWT que entrega /api/auth/login. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI linceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lince · Backend propio (Equipo 10)")
                        .version("1.0.0")
                        .description("Hitos de check-in/check-out con geocerca, incidencias, disponibilidad y "
                                + "confirmaciones. La Lince API del cliente es solo lectura: todo registro nuevo vive aquí."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
