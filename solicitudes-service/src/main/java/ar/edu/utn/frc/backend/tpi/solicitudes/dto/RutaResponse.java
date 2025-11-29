package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con información completa de la ruta.
 * Incluye todos los tramos asociados y métricas calculadas.
 * Usado en consultas de rutas y seguimiento (CU-02, CU-03).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaResponse {

    private Long id;
    private Long solicitudId;
    private Double distanciaTotalKm;
    private Double tiempoEstimadoHoras;
    private Double costoEstimado;

    @Builder.Default
    private List<TramoResponse> tramos = new ArrayList<>();

    private Integer cantidadTramos;
    private Integer cantidadDepositos;
}
