package backend_central.backend_central.controller;

import backend_central.backend_central.service.CulqiWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class CulqiWebhookController {

    private final CulqiWebhookService webhookService;

    @Value("${culqi.webhook-secret:}")
    private String webhookSecret;

    // Culqi permite configurar un header Authorization propio para el webhook en su panel; se valida contra ese secreto.
    @PostMapping("/culqi")
    public ResponseEntity<Void> recibir(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestBody Map<String, Object> payload) {
        if (webhookSecret != null && !webhookSecret.isBlank() && !webhookSecret.equals(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        webhookService.procesar(payload);
        return ResponseEntity.ok().build();
    }
}
