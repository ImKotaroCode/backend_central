package backend_central.backend_central.controller;

import backend_central.backend_central.dto.LicenciaResponse;
import backend_central.backend_central.service.LicenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/licencia")
@RequiredArgsConstructor
public class LicenciaController {

    private final LicenciaService licenciaService;

    @GetMapping("/validar/{apiKey}")
    public ResponseEntity<LicenciaResponse> validar(@PathVariable String apiKey) {
        return ResponseEntity.ok(licenciaService.validar(apiKey));
    }
}
