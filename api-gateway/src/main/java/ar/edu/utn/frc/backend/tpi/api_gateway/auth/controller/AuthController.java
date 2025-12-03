package ar.edu.utn.frc.backend.tpi.api_gateway.auth.controller;

import ar.edu.utn.frc.backend.tpi.api_gateway.auth.dto.KeycloakTokenResponse;
import ar.edu.utn.frc.backend.tpi.api_gateway.auth.dto.LoginRequest;
import ar.edu.utn.frc.backend.tpi.api_gateway.auth.service.KeycloakAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final KeycloakAuthService keycloakAuthService;

    public AuthController(KeycloakAuthService keycloakAuthService) {
        this.keycloakAuthService = keycloakAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<KeycloakTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(keycloakAuthService.login(request));
    }
}
