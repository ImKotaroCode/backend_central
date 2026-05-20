package backend_central.backend_central.service;

import backend_central.backend_central.dto.LicenciaResponse;
import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.repository.InstitucionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LicenciaService {

    private final InstitucionRepository repository;

    public LicenciaResponse validar(String apiKey) {
        var optional = repository.findByApiKey(apiKey);
        if (optional.isEmpty()) {
            return LicenciaResponse.invalida("NO_EXISTE");
        }

        Institucion inst = optional.get();

        if (inst.getEstado() == EstadoInstitucion.SUSPENDIDO) {
            return LicenciaResponse.invalida("SUSPENDIDO");
        }

        if (inst.getEstado() == EstadoInstitucion.VENCIDO) {
            return LicenciaResponse.invalida("VENCIDO");
        }

        if (inst.getFechaVencimiento().isBefore(LocalDate.now())) {
            return LicenciaResponse.invalida("VENCIDO");
        }

        return LicenciaResponse.valida(inst);
    }

    public LicenciaResponse validarPorSubdominio(String subdominio) {
        Institucion inst = repository.findBySubdominio(subdominio)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + subdominio));

        if (inst.getEstado() == EstadoInstitucion.SUSPENDIDO) {
            return LicenciaResponse.invalida("SUSPENDIDO");
        }
        if (inst.getEstado() == EstadoInstitucion.VENCIDO || inst.getFechaVencimiento().isBefore(LocalDate.now())) {
            return LicenciaResponse.invalida("VENCIDO");
        }
        return LicenciaResponse.valida(inst);
    }

    public LicenciaResponse validarPorId(Long institutionId) {
        Institucion inst = repository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + institutionId));

        if (inst.getEstado() == EstadoInstitucion.SUSPENDIDO) {
            return LicenciaResponse.invalida("SUSPENDIDO");
        }

        if (inst.getEstado() == EstadoInstitucion.VENCIDO) {
            return LicenciaResponse.invalida("VENCIDO");
        }

        if (inst.getFechaVencimiento().isBefore(LocalDate.now())) {
            return LicenciaResponse.invalida("VENCIDO");
        }

        return LicenciaResponse.valida(inst);
    }
}
