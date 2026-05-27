package backend_central.backend_central.service;

import backend_central.backend_central.dto.InstitucionRequest;
import backend_central.backend_central.dto.InstitucionResponse;
import backend_central.backend_central.dto.InstitucionUpdateRequest;
import backend_central.backend_central.dto.RenovarRequest;
import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.repository.InstitucionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitucionService {

    private final InstitucionRepository repository;
    private final RestClient.Builder restClientBuilder;

    public InstitucionResponse crear(InstitucionRequest req) {
        if (repository.existsBySubdominio(req.getSubdominio())) {
            throw new IllegalArgumentException("Subdominio ya existe: " + req.getSubdominio());
        }

        Institucion inst = new Institucion();
        inst.setNombre(req.getNombre());
        inst.setTipo(req.getTipo());
        inst.setEmailContacto(req.getEmailContacto());
        inst.setSubdominio(req.getSubdominio());
        inst.setBackendUrl(req.getBackendUrl());
        inst.setApiKey(UUID.randomUUID().toString());
        inst.setEstado(EstadoInstitucion.ACTIVO);
        inst.setFechaVencimiento(req.getFechaVencimiento());

        inst = repository.save(inst);
        Map<String, Object> onboardingResult = triggerOnboarding(inst);

        InstitucionResponse response = InstitucionResponse.from(inst);
        if (onboardingResult != null) {
            response.setAdminEmail(String.valueOf(onboardingResult.getOrDefault("email", inst.getEmailContacto())));
            Object pwd = onboardingResult.get("tempPassword");
            if (pwd != null && !pwd.toString().equals("null")) {
                response.setAdminTempPassword(pwd.toString());
            }
        }
        return response;
    }

    public List<InstitucionResponse> listar() {
        return repository.findAll().stream().map(InstitucionResponse::from).toList();
    }

    public InstitucionResponse obtener(Long id) {
        return InstitucionResponse.from(buscar(id));
    }

    public InstitucionResponse actualizar(Long id, InstitucionUpdateRequest req) {
        Institucion inst = buscar(id);
        inst.setNombre(req.getNombre());
        inst.setEmailContacto(req.getEmailContacto());
        inst.setBackendUrl(req.getBackendUrl());
        inst.setFechaVencimiento(req.getFechaVencimiento());
        return InstitucionResponse.from(repository.save(inst));
    }

    public InstitucionResponse activar(Long id) {
        Institucion inst = buscar(id);
        inst.setEstado(EstadoInstitucion.ACTIVO);
        inst = repository.save(inst);
        notificar(inst);
        return InstitucionResponse.from(inst);
    }

    public InstitucionResponse suspender(Long id) {
        Institucion inst = buscar(id);
        inst.setEstado(EstadoInstitucion.SUSPENDIDO);
        inst = repository.save(inst);
        notificar(inst);
        return InstitucionResponse.from(inst);
    }

    public InstitucionResponse renovar(Long id, RenovarRequest req) {
        Institucion inst = buscar(id);
        inst.setFechaVencimiento(req.getFechaVencimiento());
        if (inst.getEstado() == EstadoInstitucion.VENCIDO) {
            inst.setEstado(EstadoInstitucion.ACTIVO);
        }
        return InstitucionResponse.from(repository.save(inst));
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private Institucion buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + id));
    }

    private void notificar(Institucion inst) {
        if (inst.getBackendUrl() == null || inst.getBackendUrl().isBlank()) return;
        try {
            restClientBuilder.build()
                    .post()
                    .uri(inst.getBackendUrl() + "/api/internal/licencia/notificacion")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", inst.getEstado().name(), "institutionId", inst.getId()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notificación fallida para {} ({}): {}", inst.getNombre(), inst.getId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> triggerOnboarding(Institucion inst) {
        if (inst.getBackendUrl() == null || inst.getBackendUrl().isBlank()) return null;
        try {
            Map<String, Object> result = restClientBuilder.build()
                    .post()
                    .uri(inst.getBackendUrl() + "/api/internal/onboarding")
                    .header("X-API-KEY", inst.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("admin_email", inst.getEmailContacto()))
                    .retrieve()
                    .body(Map.class);
            log.info("Onboarding disparado para {} (id={}): {}", inst.getNombre(), inst.getId(), result);
            return result;
        } catch (Exception e) {
            log.warn("Onboarding fallido para {} ({}): {}", inst.getNombre(), inst.getId(), e.getMessage());
            return null;
        }
    }
}
