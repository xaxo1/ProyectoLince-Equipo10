package cl.duoc.lince.repositorio;

import cl.duoc.lince.modelo.Enums;
import cl.duoc.lince.modelo.Hito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HitoRepositorio extends JpaRepository<Hito, Long> {
    Optional<Hito> findByIdempotencia(String idempotencia);
    List<Hito> findByUsuarioIdOrderByFechaHoraServidorDesc(Long usuarioId);
    List<Hito> findByServicioIdAndUsuarioIdOrderByFechaHoraServidorAsc(int servicioId, Long usuarioId);
    boolean existsByServicioIdAndUsuarioIdAndTipo(int servicioId, Long usuarioId, Enums.TipoHito tipo);
    List<Hito> findByFechaHoraServidorBetweenOrderByFechaHoraServidorDesc(LocalDateTime desde, LocalDateTime hasta);
    Optional<Hito> findFirstByUsuarioIdAndTipoOrderByFechaHoraServidorDesc(Long usuarioId, Enums.TipoHito tipo);
}
