package backend_central.backend_central.service;

import backend_central.backend_central.dto.CheckoutRequest;
import backend_central.backend_central.dto.CheckoutResponse;
import backend_central.backend_central.entity.Institucion;
import backend_central.backend_central.enums.EstadoInstitucion;
import backend_central.backend_central.exception.CheckoutException;
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

    private static final String PLAN_ROCKET = "ROCKET";
    private static final String PLAN_INICIAL = "INICIAL";
    private static final String PAGO_CARD = "card";
    private static final String PAGO_YAPE = "yape";

    private static final int ALUMNOS_BASE = 250;
    private static final int BASE_ROCKET = 200;
    private static final int BASE_INICIAL = 40;

    private final InstitucionRepository repository;
    private final CulqiService culqiService;
    private final EmailService emailService;

    @Value("${app.membresia-url}")
    private String membresiaUrl;

    @Value("${app.kui-trial-meses:6}")
    private int trialMesesRocket;

    public CheckoutResponse procesar(CheckoutRequest req) {
        String plan = normalizarPlan(req.getPlan());
        String metodoPago = normalizarMetodoPago(req.getMetodoPago());

        if (PLAN_ROCKET.equals(plan) && PAGO_YAPE.equals(metodoPago)) {
            throw new CheckoutException("El plan Rocket solo admite pago con tarjeta.");
        }

        int montoMensual = calcularMontoMensual(plan, req.getAlumnos());
        // El trial lo define el plan en el servidor, no el cliente.
        int trialMeses = PLAN_ROCKET.equals(plan) ? trialMesesRocket : 0;
        int montoHoy = trialMeses > 0 ? 0 : montoMensual;

        String nombreInstitucion = req.getInstitucion().getNombre();
        String emailInstitucion = req.getInstitucion().getEmailContacto();

        String customerId = culqiService.crearCliente(nombreInstitucion, emailInstitucion, req.getContacto().getTelefono());
        String cardId = null;
        String subscriptionId = null;
        boolean requiereMedioRecurrente = false;
        LocalDate fechaVencimiento;

        if (PAGO_YAPE.equals(metodoPago)) {
            // Yape es pago unico: no deja tarjeta para cobros recurrentes.
            culqiService.crearCargo(
                    req.getCulqiToken(),
                    emailInstitucion,
                    montoHoy * 100,
                    "KUI Inicial " + req.getAlumnos() + " alumnos - primer mes"
            );
            requiereMedioRecurrente = true;
            fechaVencimiento = LocalDate.now().plusMonths(1);
        } else {
            cardId = culqiService.crearTarjeta(customerId, req.getCulqiToken());
            String planId = culqiService.crearPlan(
                    "KUI " + plan + " " + req.getAlumnos() + " alumnos",
                    montoMensual * 100,
                    trialMeses * 30
            );
            subscriptionId = culqiService.crearSuscripcion(cardId, planId);
            fechaVencimiento = LocalDate.now().plusMonths(trialMeses > 0 ? trialMeses : 1);
        }

        Institucion inst = new Institucion();
        inst.setNombre(nombreInstitucion);
        inst.setTipo(req.getInstitucion().getTipo());
        inst.setEmailContacto(emailInstitucion);
        inst.setSubdominio("pendiente-" + UUID.randomUUID());
        inst.setApiKey(UUID.randomUUID().toString());
        inst.setEstado(EstadoInstitucion.ACTIVO);
        inst.setFechaVencimiento(fechaVencimiento);
        inst.setRuc(req.getInstitucion().getRuc());
        inst.setDominioDeseado(req.getInstitucion().getDominioDeseado());
        inst.setCulqiCustomerId(customerId);
        inst.setCulqiCardId(cardId);
        inst.setCulqiSubscriptionId(subscriptionId);
        inst.setAlumnosContratados(req.getAlumnos());
        inst.setMontoMensual(montoMensual);
        inst.setPlan(plan);
        inst.setRequiereMedioRecurrente(requiereMedioRecurrente);

        inst = repository.save(inst);
        log.info("Institucion creada via checkout publico: id={}, plan={}, metodoPago={}, subscriptionId={}",
                inst.getId(), plan, metodoPago, subscriptionId);

        String loginUrl = membresiaUrl + "?welcome=1&inst=" + inst.getId();
        emailService.enviarBienvenida(inst, req.getContacto().getNombreCompleto(), loginUrl);

        return new CheckoutResponse(true, inst.getId(), loginUrl);
    }

    // max(base, base + (alumnos - 250)) soles/mes; base ROCKET=200, INICIAL=40
    public int calcularMontoMensual(String plan, int alumnos) {
        int base = PLAN_ROCKET.equals(plan) ? BASE_ROCKET : BASE_INICIAL;
        return Math.max(base, base + (alumnos - ALUMNOS_BASE));
    }

    private String normalizarPlan(String plan) {
        String p = plan == null || plan.isBlank() ? PLAN_ROCKET : plan.trim().toUpperCase();
        if (!PLAN_ROCKET.equals(p) && !PLAN_INICIAL.equals(p)) {
            throw new CheckoutException("Plan no valido: " + plan);
        }
        return p;
    }

    private String normalizarMetodoPago(String metodoPago) {
        String m = metodoPago == null || metodoPago.isBlank() ? PAGO_CARD : metodoPago.trim().toLowerCase();
        if (!PAGO_CARD.equals(m) && !PAGO_YAPE.equals(m)) {
            throw new CheckoutException("Metodo de pago no valido: " + metodoPago);
        }
        return m;
    }
}
