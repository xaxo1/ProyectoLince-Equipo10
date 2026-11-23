package cl.duoc.lince.config;

import cl.duoc.lince.modelo.Enums.Rol;
import cl.duoc.lince.modelo.Geocerca;
import cl.duoc.lince.modelo.Usuario;
import cl.duoc.lince.repositorio.GeocercaRepositorio;
import cl.duoc.lince.repositorio.UsuarioRepositorio;
import cl.duoc.lince.servicio.Reglas;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos ficticios la PRIMERA vez que arranca (base vacía): usuarios de prueba y el
 * catálogo de geocercas de los 14 lugares de pickup/dropoff que usa la Lince API.
 * Las coordenadas son sintéticas (cerca de cada barrio), no ubicaciones reales del cliente.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepositorio usuarios;
    private final GeocercaRepositorio geocercas;
    private final PasswordEncoder encoder;
    private final Reglas reglas;

    public DatosIniciales(UsuarioRepositorio usuarios, GeocercaRepositorio geocercas,
                          PasswordEncoder encoder, Reglas reglas) {
        this.usuarios = usuarios;
        this.geocercas = geocercas;
        this.encoder = encoder;
        this.reglas = reglas;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() == 0) {
            String clave = encoder.encode("1234");
            // linceRecursoId = id del guía / transportista en la Lince API del cliente
            usuarios.save(new Usuario("guia@southbound.cl", "15482910K", "Gonzalo Orellana Sandoval", clave, Rol.GUIA, 37));
            usuarios.save(new Usuario("transportista@southbound.cl", "123456785", "Nicolás Escobar Campos", clave, Rol.TRANSPORTISTA, 2));
            usuarios.save(new Usuario("coordinacion@southbound.cl", "111111111", "Coordinación Santiago", clave, Rol.COORDINADOR, null));
        }
        if (geocercas.count() == 0) {
            int r = reglas.radioDefecto();
            // Santiago (SCL)
            geocercas.save(new Geocerca("Aeropuerto Internacional SCL", "SCL", -33.39300, -70.78580, r));
            geocercas.save(new Geocerca("Hotel Barrio Lastarria", "SCL", -33.43780, -70.64050, r));
            geocercas.save(new Geocerca("Hotel Centro Santiago", "SCL", -33.44200, -70.65300, r));
            geocercas.save(new Geocerca("Hotel Las Condes", "SCL", -33.41450, -70.59400, r));
            geocercas.save(new Geocerca("Hotel Providencia", "SCL", -33.42550, -70.61100, r));
            geocercas.save(new Geocerca("Hotel Vitacura", "SCL", -33.39900, -70.59600, r));
            // Puerto Varas (PUX)
            geocercas.save(new Geocerca("Aeropuerto El Tepual", "PUX", -41.43890, -73.09400, r));
            geocercas.save(new Geocerca("Hostería Lago Llanquihue", "PUX", -41.31800, -72.98500, r));
            geocercas.save(new Geocerca("Hotel Centro Puerto Varas", "PUX", -41.31750, -72.98950, r));
            geocercas.save(new Geocerca("Hotel Costanera Puerto Varas", "PUX", -41.32100, -72.97900, r));
            // Punta Arenas (PUQ)
            geocercas.save(new Geocerca("Aeropuerto Presidente Ibáñez", "PUQ", -53.00260, -70.85460, r));
            geocercas.save(new Geocerca("Hotel Centro Punta Arenas", "PUQ", -53.16300, -70.90800, r));
            geocercas.save(new Geocerca("Hotel Costanera Punta Arenas", "PUQ", -53.15600, -70.90000, r));
            geocercas.save(new Geocerca("Hotel Plaza de Armas", "PUQ", -53.16400, -70.91100, r));
        }
    }
}
