package ar.edu.utn.frc.backend.tpi.transportes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con información detallada del tramo.
 * Incluye fechas reales, costos y estado actual.
 * Usado en seguimiento de envíos (CU-02, CU-12).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoResponse {

    private Long id;
    private Long rutaId;
    private String origen;
    private String destino;
    private String tipo;
    private String estado;
    private Double costoEstimado;
    private Double costoReal;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Long camionId;
}
