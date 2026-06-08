package backend_central.backend_central.service;

import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.repository.InstitucionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final InstitucionRepository repository;
    private final RestClient.Builder restClientBuilder;

    @Value("${app.self-url:https://backend-central-lo7f.onrender.com}")
    private String selfUrl;

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

    // Cada 2 minutos para evitar que Render duerma el servicio
    @Scheduled(fixedRate = 120_000)
    public void keepAlive() {
        try {
            String response = restClientBuilder.build()
                    .get()
                    .uri(selfUrl + "/api/health")
                    .retrieve()
                    .body(String.class);
            log.debug("Keep-alive ping OK: {}", response);
        } catch (Exception e) {
            log.warn("Keep-alive ping falló: {}", e.getMessage());
        }
    }
}
