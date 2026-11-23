package cl.duoc.lince.repositorio;

import cl.duoc.lince.modelo.Geocerca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeocercaRepositorio extends JpaRepository<Geocerca, Long> {
    Optional<Geocerca> findByNombreIgnoreCase(String nombre);
}
