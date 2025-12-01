package ar.edu.utn.frc.backend.tpi.costos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TarifaDto {
    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    private Double costoBaseKm;

    @NotNull
    private Double valorCombustibleLitro;

    @NotNull
    private Double costoEstadiaDiaria;

    @NotNull
    private Double velocidadPromedioKmH;

    @NotNull
    private Double costoGestionFijo;
}
