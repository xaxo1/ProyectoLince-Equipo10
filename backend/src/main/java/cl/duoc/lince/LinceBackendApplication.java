package cl.duoc.lince;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada del microservicio Lince (backend propio del equipo 10). */
@SpringBootApplication
public class LinceBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinceBackendApplication.class, args);
    }
}
