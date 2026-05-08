package backend_central.backend_central.controller;

import backend_central.backend_central.dto.InstitucionRequest;
import backend_central.backend_central.dto.InstitucionResponse;
import backend_central.backend_central.dto.InstitucionUpdateRequest;
import backend_central.backend_central.dto.RenovarRequest;
import backend_central.backend_central.service.InstitucionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instituciones")
@RequiredArgsConstructor
public class InstitucionController {

    private final InstitucionService service;

    @PostMapping
    public ResponseEntity<InstitucionResponse> crear(@Valid @RequestBody InstitucionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<InstitucionResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitucionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstitucionResponse> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody InstitucionUpdateRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<InstitucionResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(service.activar(id));
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<InstitucionResponse> suspender(@PathVariable Long id) {
        return ResponseEntity.ok(service.suspender(id));
    }

    @PatchMapping("/{id}/renovar")
    public ResponseEntity<InstitucionResponse> renovar(@PathVariable Long id,
                                                       @Valid @RequestBody RenovarRequest request) {
        return ResponseEntity.ok(service.renovar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
