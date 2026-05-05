package backend_central.backend_central.service;

import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.repository.InstitucionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final InstitucionRepository repository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void verificarVencimientos() {
        var vencidas = repository.findByEstadoAndFechaVencimientoBefore(
                EstadoInstitucion.ACTIVO, LocalDate.now()
        );

        vencidas.forEach(inst -> {
            inst.setEstado(EstadoInstitucion.VENCIDO);
            repository.save(inst);
            log.info("Institución vencida: {} ({})", inst.getNombre(), inst.getId());
        });

        log.info("Verificación vencimientos: {} instituciones actualizadas", vencidas.size());
    }
}
