package backend_central.backend_central.dto;

import backend_central.backend_central.entity.Institucion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenciaResponse {
    private boolean valida;
    private String motivo;
    private Long id;
    private String nombre;
    private String estado;
    private String apiKey;
    private String backendUrl;

    public static LicenciaResponse valida(Institucion inst) {
        return new LicenciaResponse(
            true, null,
            inst.getId(), inst.getNombre(), inst.getEstado().name(),
            inst.getApiKey(), inst.getBackendUrl()
        );
    }

    public static LicenciaResponse invalida(String motivo) {
        return new LicenciaResponse(false, motivo, null, null, null, null, null);
    }
}
