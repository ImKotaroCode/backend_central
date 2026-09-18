package backend_central.backend_central.service;

import backend_central.backend_central.dto.CheckoutRequest;
import backend_central.backend_central.dto.CheckoutResponse;
import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.repository.InstitucionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private static final int ALUMNOS_BASE = 250;
    private static final int MONTO_BASE = 200;
    private static final int MONTO_MINIMO = 200;

    private final InstitucionRepository repository;
    private final CulqiService culqiService;
    private final EmailService emailService;

    @Value("${app.membresia-url}")
    private String membresiaUrl;

    @Value("${app.kui-trial-meses:6}")
    private int trialMesesDefault;

    public CheckoutResponse procesar(CheckoutRequest req) {
        int montoMensual = calcularMontoMensual(req.getAlumnos());
        int trialMeses = req.getTrialMeses() != null ? req.getTrialMeses() : trialMesesDefault;
        int montoCentimos = montoMensual * 100;

        String nombreInstitucion = req.getInstitucion().getNombre();
        String emailInstitucion = req.getInstitucion().getEmailContacto();

        String customerId = culqiService.crearCliente(nombreInstitucion, emailInstitucion, req.getContacto().getTelefono());
        String cardId = culqiService.crearTarjeta(customerId, req.getCulqiToken());
        String planId = culqiService.crearPlan(
                "KUI Rocket " + req.getAlumnos() + " alumnos",
                montoCentimos,
                trialMeses * 30
        );
        String subscriptionId = culqiService.crearSuscripcion(cardId, planId);

        Institucion inst = new Institucion();
        inst.setNombre(nombreInstitucion);
        inst.setTipo(req.getInstitucion().getTipo());
        inst.setEmailContacto(emailInstitucion);
        inst.setSubdominio("pendiente-" + UUID.randomUUID());
        inst.setApiKey(UUID.randomUUID().toString());
        inst.setEstado(EstadoInstitucion.ACTIVO);
        inst.setFechaVencimiento(LocalDate.now().plusMonths(trialMeses));
        inst.setRuc(req.getInstitucion().getRuc());
        inst.setDominioDeseado(req.getInstitucion().getDominioDeseado());
        inst.setCulqiCustomerId(customerId);
        inst.setCulqiCardId(cardId);
        inst.setCulqiSubscriptionId(subscriptionId);
        inst.setAlumnosContratados(req.getAlumnos());
        inst.setMontoMensual(montoMensual);

        inst = repository.save(inst);
        log.info("Institucion creada via checkout publico: id={}, subscriptionId={}", inst.getId(), subscriptionId);

        String loginUrl = membresiaUrl + "?welcome=1&inst=" + inst.getId();
        emailService.enviarBienvenida(inst, req.getContacto().getNombreCompleto(), loginUrl);

        return new CheckoutResponse(true, inst.getId(), loginUrl);
    }

    // max(200, 200 + (alumnos - 250)) soles/mes
    public int calcularMontoMensual(int alumnos) {
        return Math.max(MONTO_MINIMO, MONTO_BASE + (alumnos - ALUMNOS_BASE));
    }
}
