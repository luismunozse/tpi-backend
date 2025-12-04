package ar.edu.utn.frc.backend.tpi.flota.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransportistaDto {
    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String dni;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String telefono;

    @NotBlank
    private String numeroLicencia;

    @NotBlank
    private String categoriaLicencia;

    @NotNull
    private Boolean activo;
}
