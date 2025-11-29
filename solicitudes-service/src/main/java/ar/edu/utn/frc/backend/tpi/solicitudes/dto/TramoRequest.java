package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de tramos dentro de una ruta.
 * Usado por operadores/administradores al armar rutas tentativas (CU-03).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoRequest {

    @NotNull(message = "El ID de la ruta es obligatorio")
    private Long rutaId;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotBlank(message = "El destino es obligatorio")
    private String destino;

    @NotBlank(message = "El tipo de tramo es obligatorio")
    private String tipo; // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

    private Double costoEstimado;
}
