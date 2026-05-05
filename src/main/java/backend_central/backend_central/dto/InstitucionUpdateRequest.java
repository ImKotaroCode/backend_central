package backend_central.backend_central.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InstitucionUpdateRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String emailContacto;

    @NotBlank
    private String backendUrl;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimiento;
}
