package ar.edu.utn.frc.backend.tpi.costos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstimacionCostoRequest {

    @NotNull
    private Double distanciaKm;

    @NotNull
    private Double costoBaseKmCamion;

    @NotNull
    private Double consumoCamionLitrosKm;

    @NotNull
    private Double diasEstadia;

    @NotNull
    private Double costoEstadiaDiaria;

    // Opcional: id de la tarifa a aplicar; si es nulo se usa la primera disponible.
    private Long tarifaId;
}
