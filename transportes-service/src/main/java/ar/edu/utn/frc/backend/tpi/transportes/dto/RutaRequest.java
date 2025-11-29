package ar.edu.utn.frc.backend.tpi.transportes.dto;

import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de rutas completas con sus tramos.
 * Usado por operadores al generar rutas tentativas (CU-03).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaRequest {

    @NotNull(message = "La distancia total es obligatoria")
    @DecimalMin(value = "0.1", message = "La distancia debe ser mayor a 0")
    private Double distanciaTotalKm;

    @NotNull(message = "El tiempo estimado es obligatorio")
    @DecimalMin(value = "0.1", message = "El tiempo estimado debe ser mayor a 0")
    private Double tiempoEstimadoHoras;

    @NotNull(message = "El costo estimado es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo estimado no puede ser negativo")
    private Double costoEstimado;

    private List<TramoRequest> tramos;
}
