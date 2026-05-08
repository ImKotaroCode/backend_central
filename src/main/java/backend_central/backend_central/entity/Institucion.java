package backend_central.backend_central.entity;

import backend_central.backend_central.enums.EstadoInstitucion;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instituciones")
@Data
public class Institucion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String emailContacto;

    @Column(unique = true, nullable = false)
    private String subdominio;

    @Column(nullable = false)
    private String backendUrl;

    @Column(unique = true, nullable = false)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInstitucion estado;

    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoInstitucion.ACTIVO;
    }
}
