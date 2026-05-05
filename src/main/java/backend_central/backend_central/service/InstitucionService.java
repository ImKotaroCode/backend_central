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

        return InstitucionResponse.from(repository.save(inst));
    }

    public List<InstitucionResponse> listar() {
        return repository.findAll().stream().map(InstitucionResponse::from).toList();
    }

    public InstitucionResponse obtener(UUID id) {
        return InstitucionResponse.from(buscar(id));
    }

    public InstitucionResponse actualizar(UUID id, InstitucionUpdateRequest req) {
        Institucion inst = buscar(id);
        inst.setNombre(req.getNombre());
        inst.setEmailContacto(req.getEmailContacto());
        inst.setBackendUrl(req.getBackendUrl());
        inst.setFechaVencimiento(req.getFechaVencimiento());
        return InstitucionResponse.from(repository.save(inst));
    }

    public InstitucionResponse activar(UUID id) {
        Institucion inst = buscar(id);
        inst.setEstado(EstadoInstitucion.ACTIVO);
        inst = repository.save(inst);
        notificar(inst);
        return InstitucionResponse.from(inst);
    }

    public InstitucionResponse suspender(UUID id) {
        Institucion inst = buscar(id);
        inst.setEstado(EstadoInstitucion.SUSPENDIDO);
        inst = repository.save(inst);
        notificar(inst);
        return InstitucionResponse.from(inst);
    }

    public InstitucionResponse renovar(UUID id, RenovarRequest req) {
        Institucion inst = buscar(id);
        inst.setFechaVencimiento(req.getFechaVencimiento());
        if (inst.getEstado() == EstadoInstitucion.VENCIDO) {
            inst.setEstado(EstadoInstitucion.ACTIVO);
        }
        return InstitucionResponse.from(repository.save(inst));
    }

    public void eliminar(UUID id) {
        repository.delete(buscar(id));
    }

    private Institucion buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + id));
    }

    private void notificar(Institucion inst) {
        if (inst.getBackendUrl() == null || inst.getBackendUrl().isBlank()) return;
        try {
            restClientBuilder.build()
                    .post()
                    .uri(inst.getBackendUrl() + "/api/licencia/notificacion")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", inst.getEstado().name()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notificación fallida para {} ({}): {}", inst.getNombre(), inst.getId(), e.getMessage());
        }
    }
}
