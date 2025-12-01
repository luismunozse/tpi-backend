package ar.edu.utn.frc.backend.tpi.flota.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionCamionRequest {
    @NotNull
    private Double pesoKg;

    @NotNull
    private Double volumenM3;
}
