package backend_central.backend_central.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank
    private String culqiToken;

    private String plan = "ROCKET";

    private Integer trialMeses = 6;

    @NotNull
    @Positive
    private Integer alumnos;

    // Informativo; el backend recalcula el monto real, no confia en este valor
    private Integer montoMensual;

    @NotNull
    @Valid
    private ContactoRequest contacto;

    @NotNull
    @Valid
    private InstitucionCheckoutRequest institucion;

    @Data
    public static class ContactoRequest {
        @NotBlank
        private String nombreCompleto;

        @NotBlank
        @Email
        private String email;

        private String cargo;

        private String telefono;
    }

    @Data
    public static class InstitucionCheckoutRequest {
        @NotBlank
        private String nombre;

        @NotBlank
        private String tipo;

        // Texto libre; el equipo KUI provisiona el dominio real despues
        private String dominioDeseado;

        private String ruc;

        @NotBlank
        @Email
        private String emailContacto;
    }
}
