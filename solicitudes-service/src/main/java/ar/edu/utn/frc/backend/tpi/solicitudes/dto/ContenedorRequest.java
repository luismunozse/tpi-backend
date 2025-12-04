package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de contenedores.
 * Incluye el cliente asociado al contenedor.
 * Usado en CU-01: Registrar solicitud de transporte.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorRequest {

    @NotBlank(message = "El número de serie es obligatorio")
    private String numeroSerie;

    @NotBlank(message = "El tipo de contenedor es obligatorio")
    private String tipo;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "0.1", message = "El peso debe ser mayor a 0")
    private Double peso;

    @NotNull(message = "El volumen es obligatorio")
    @DecimalMin(value = "0.1", message = "El volumen debe ser mayor a 0")
    private Double volumen;

    // Se setea internamente tras crear/obtener el cliente asociado; puede venir nulo en el request.
    private Long clienteId;
}
