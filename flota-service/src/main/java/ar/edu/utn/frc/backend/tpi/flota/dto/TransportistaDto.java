package ar.edu.utn.frc.backend.tpi.flota.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para transferencia de datos de Transportista.
 */
@Data
@Builder
public class TransportistaDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El número de licencia es obligatorio")
    private String numeroLicencia;

    @NotBlank(message = "La categoría de licencia es obligatoria")
    private String categoriaLicencia; // A, B, C, D, E

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    // Número de camiones asignados (opcional, solo para consultas)
    private Integer cantidadCamionesAsignados;
}
