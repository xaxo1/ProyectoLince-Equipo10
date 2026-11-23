package cl.duoc.lince.repositorio;

import cl.duoc.lince.modelo.Confirmacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmacionRepositorio extends JpaRepository<Confirmacion, Long> {
    List<Confirmacion> findByUsuarioId(Long usuarioId);
    Optional<Confirmacion> findByUsuarioIdAndServicioId(Long usuarioId, int servicioId);
}
