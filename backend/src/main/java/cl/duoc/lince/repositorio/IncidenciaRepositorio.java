package cl.duoc.lince.repositorio;

import cl.duoc.lince.modelo.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenciaRepositorio extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
    List<Incidencia> findByServicioIdOrderByFechaHoraDesc(int servicioId);
    List<Incidencia> findAllByOrderByFechaHoraDesc();
}
