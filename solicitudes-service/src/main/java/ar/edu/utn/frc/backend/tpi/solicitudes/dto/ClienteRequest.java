package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación y actualización de clientes.
 * Usado en el flujo de registro de clientes (CU-01).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "El teléfono debe tener entre 10 y 15 dígitos")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
}
