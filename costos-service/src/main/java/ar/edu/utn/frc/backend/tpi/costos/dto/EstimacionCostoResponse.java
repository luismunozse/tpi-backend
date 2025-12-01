package ar.edu.utn.frc.backend.tpi.costos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstimacionCostoResponse {

    private Double distanciaKm;
    private Double costoKilometraje;
    private Double costoCombustible;
    private Double costoEstadia;
    private Double costoTotal;
    private Double tiempoEstimadoHoras;
}
