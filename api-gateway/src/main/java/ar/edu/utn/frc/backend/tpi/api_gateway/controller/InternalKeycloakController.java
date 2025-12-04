package ar.edu.utn.frc.backend.tpi.api_gateway.controller;

import ar.edu.utn.frc.backend.tpi.api_gateway.service.KeycloakAdminService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint interno para aprovisionar usuarios CLIENTE en Keycloak.
 * Destinado a ser llamado por otros microservicios.
 */
@RestController
@RequestMapping("/internal/keycloak")
@RequiredArgsConstructor
public class InternalKeycloakController {

    private final KeycloakAdminService keycloakAdminService;

    @PostMapping("/users")
    public ResponseEntity<Void> crearUsuarioCliente(@RequestBody CrearUsuarioRequest request) {
        keycloakAdminService.ensureClientUser(request.getNombre(), request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @Data
    public static class CrearUsuarioRequest {
        private String nombre;
        private String email;
    }
}
