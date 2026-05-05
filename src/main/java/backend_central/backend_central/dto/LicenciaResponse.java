package backend_central.backend_central.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LicenciaResponse {
    private boolean valida;
    private String motivo;

    public static LicenciaResponse valida() {
        return new LicenciaResponse(true, null);
    }

    public static LicenciaResponse invalida(String motivo) {
        return new LicenciaResponse(false, motivo);
    }
}
