package ar.edu.utn.frc.backend.tpi.api_gateway.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String username,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
