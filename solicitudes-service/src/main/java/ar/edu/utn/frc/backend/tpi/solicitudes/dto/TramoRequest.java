package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de tramos dentro de una ruta.
 * Usado por operadores/administradores al armar rutas tentativas (CU-03).
 * El rutaId es opcional ya que se asigna automáticamente al crear la ruta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoRequest {

    // rutaId es opcional: se asigna automáticamente al crear la ruta con sus tramos
    private Long rutaId;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotBlank(message = "El destino es obligatorio")
    private String destino;

    @NotBlank(message = "El tipo de tramo es obligatorio")
    private String tipo; // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

    private Double costoEstimado;
}
