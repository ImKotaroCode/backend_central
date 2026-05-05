package backend_central.backend_central.dto;

import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InstitucionResponse {
    private UUID id;
    private String nombre;
    private String tipo;
    private String emailContacto;
    private String subdominio;
    private String backendUrl;
    private String apiKey;
    private EstadoInstitucion estado;
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimiento;

    public static InstitucionResponse from(Institucion i) {
        InstitucionResponse r = new InstitucionResponse();
        r.setId(i.getId());
        r.setNombre(i.getNombre());
        r.setTipo(i.getTipo());
        r.setEmailContacto(i.getEmailContacto());
        r.setSubdominio(i.getSubdominio());
        r.setBackendUrl(i.getBackendUrl());
        r.setApiKey(i.getApiKey());
        r.setEstado(i.getEstado());
        r.setFechaCreacion(i.getFechaCreacion());
        r.setFechaVencimiento(i.getFechaVencimiento());
        return r;
    }
}
