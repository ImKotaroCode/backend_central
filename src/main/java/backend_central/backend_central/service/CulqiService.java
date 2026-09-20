package backend_central.backend_central.service;

import backend_central.backend_central.exception.CheckoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

// Integracion con la API de Culqi (clientes, tarjetas, planes y suscripciones recurrentes).
@Service
@Slf4j
public class CulqiService {

    private final RestClient.Builder restClientBuilder;

    @Value("${culqi.secret-key}")
    private String secretKey;

    @Value("${culqi.api-url}")
    private String apiUrl;

    public CulqiService(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public String crearCliente(String nombreInstitucion, String email, String telefono) {
        Map<String, Object> body = Map.of(
                "first_name", nombreInstitucion,
                "last_name", "KUI",
                "email", email,
                "address", "Lima",
                "address_city", "Lima",
                "country_code", "PE",
                "phone_number", telefono == null || telefono.isBlank() ? "999999999" : telefono
        );
        Map<String, Object> result = post("/customers", body);
        return (String) result.get("id");
    }

    @SuppressWarnings("unchecked")
    public String crearTarjeta(String customerId, String tokenId) {
        Map<String, Object> body = Map.of("customer_id", customerId, "token_id", tokenId);
        Map<String, Object> result = post("/cards", body);
        return (String) result.get("id");
    }

    public String crearPlan(String nombre, int montoCentimos, int trialDias) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", nombre);
        body.put("amount", montoCentimos);
        body.put("currency_code", "PEN");
        body.put("interval_unit_time", 3);
        body.put("interval_count", 1);
        if (trialDias > 0) {
            body.put("trial_days", trialDias);
        }
        Map<String, Object> result = post("/recurrent/plans", body);
        return (String) result.get("id");
    }

    // Cargo unico inmediato (token Yape o tarjeta). source_id es el token de Culqi.
    public String crearCargo(String tokenId, String email, int montoCentimos, String descripcion) {
        Map<String, Object> body = Map.of(
                "amount", montoCentimos,
                "currency_code", "PEN",
                "email", email,
                "source_id", tokenId,
                "description", descripcion
        );
        Map<String, Object> result = post("/charges", body);
        return (String) result.get("id");
    }

    @SuppressWarnings("unchecked")
    public String crearSuscripcion(String cardId, String planId) {
        Map<String, Object> body = Map.of("card_id", cardId, "plan_id", planId, "tyc", true);
        Map<String, Object> result = post("/recurrent/subscriptions/create", body);
        return (String) result.get("id");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, Object> body) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(apiUrl + path)
                    .header("Authorization", "Bearer " + secretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException e) {
            log.warn("Culqi {} fallo ({}): {}", path, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CheckoutException(mensajeCulqi(e), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String mensajeCulqi(RestClientResponseException e) {
        try {
            Map<String, Object> body = e.getResponseBodyAs(Map.class);
            if (body != null && body.get("user_message") != null) {
                return String.valueOf(body.get("user_message"));
            }
            if (body != null && body.get("merchant_message") != null) {
                return String.valueOf(body.get("merchant_message"));
            }
        } catch (Exception ignored) {
        }
        return "No se pudo procesar el pago. Verifica los datos de tu tarjeta.";
    }
}
