package ar.edu.utn.frc.backend.tpi.flota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositoDto {
    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String direccion;

    @NotNull
    private Double latitud;

    @NotNull
    private Double longitud;

    @NotNull
    private Double costoEstadiaDiaria;
}
