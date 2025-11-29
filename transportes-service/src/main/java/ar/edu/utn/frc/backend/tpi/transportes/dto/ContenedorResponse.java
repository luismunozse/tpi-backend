package ar.edu.utn.frc.backend.tpi.transportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con información del contenedor.
 * Incluye estado y datos del cliente propietario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorResponse {

    private Long id;
    private String numeroSerie;
    private String tipo;
    private Double peso;
    private Double volumen;
    private String estado;
    private Long clienteId;
    private String clienteNombre;
}
