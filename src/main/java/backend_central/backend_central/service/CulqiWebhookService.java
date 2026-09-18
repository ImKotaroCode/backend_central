package backend_central.backend_central.service;

import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.repository.InstitucionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

// Procesa eventos de cobro recurrente de Culqi para mantener la institucion al dia.
// Nota: ajustar la extraccion de campos al payload real una vez confirmado en el panel de Culqi.
@Service
@RequiredArgsConstructor
@Slf4j
public class CulqiWebhookService {

    private final InstitucionRepository repository;

    @SuppressWarnings("unchecked")
    public void procesar(Map<String, Object> payload) {
        String tipo = String.valueOf(payload.getOrDefault("type", ""));
        Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
        String subscriptionId = extraerSubscriptionId(data);

        if (subscriptionId == null) {
            log.warn("Webhook Culqi sin subscription id identificable: {}", payload);
            return;
        }

        Institucion inst = repository.findByCulqiSubscriptionId(subscriptionId).orElse(null);
        if (inst == null) {
            log.warn("Webhook Culqi para suscripcion desconocida: {}", subscriptionId);
            return;
        }

        if (tipo.toLowerCase().contains("fail") || tipo.toLowerCase().contains("cancel")) {
            inst.setEstado(EstadoInstitucion.SUSPENDIDO);
            repository.save(inst);
            log.info("Institucion {} suspendida por evento Culqi {}", inst.getId(), tipo);
        } else if (tipo.toLowerCase().contains("succeed") || tipo.toLowerCase().contains("paid") || tipo.toLowerCase().contains("success")) {
            inst.setEstado(EstadoInstitucion.ACTIVO);
            inst.setFechaVencimiento(LocalDate.now().plusMonths(1));
            repository.save(inst);
            log.info("Institucion {} renovada por evento Culqi {}", inst.getId(), tipo);
        } else {
            log.info("Evento Culqi ignorado ({}) para suscripcion {}", tipo, subscriptionId);
        }
    }

    private String extraerSubscriptionId(Map<String, Object> data) {
        Object v = data.get("subscription_id");
        if (v == null) v = data.get("id");
        return v == null ? null : String.valueOf(v);
    }
}
