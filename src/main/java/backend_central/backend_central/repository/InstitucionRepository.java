package backend_central.backend_central.repository;

import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitucionRepository extends JpaRepository<Institucion, Long> {
    boolean existsBySubdominio(String subdominio);
    Optional<Institucion> findByApiKey(String apiKey);
    List<Institucion> findByEstadoAndFechaVencimientoBefore(EstadoInstitucion estado, LocalDate fecha);
}
