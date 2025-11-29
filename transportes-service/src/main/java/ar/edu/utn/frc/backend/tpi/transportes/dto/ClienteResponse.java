package ar.edu.utn.frc.backend.tpi.transportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con información del cliente.
 * Usado para devolver datos de clientes en consultas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
}
