package cl.duoc.lince.repositorio;

import cl.duoc.lince.modelo.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BloqueoRepositorio extends JpaRepository<Bloqueo, Long> {
    List<Bloqueo> findByUsuarioIdOrderByFechaAsc(Long usuarioId);
    Optional<Bloqueo> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
