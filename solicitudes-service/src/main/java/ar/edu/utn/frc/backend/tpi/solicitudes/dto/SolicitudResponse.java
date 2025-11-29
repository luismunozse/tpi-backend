package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta completo para solicitudes de transporte.
 * Incluye información del cliente, contenedor, ruta y métricas.
 * Usado en todos los casos de consulta (CU-02, CU-05, CU-12).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponse {

    // Datos básicos
    private Long id;
    private String estado;
    private LocalDateTime fechaCreacion;

    // Información del cliente
    private Long clienteId;
    private String clienteNombre;
    private String clienteEmail;

    // Información del contenedor
    private Long contenedorId;
    private String contenedorNumeroSerie;
    private Double contenedorPeso;
    private Double contenedorVolumen;

    // Ubicaciones
    private String origenDireccion;
    private Double origenLatitud;
    private Double origenLongitud;
    private String destinoDireccion;
    private Double destinoLatitud;
    private Double destinoLongitud;

    // Ruta asociada (puede ser null si no fue asignada)
    private Long rutaId;
    private RutaResponse ruta;

    // Métricas estimadas
    private Double costoEstimado;
    private Double tiempoEstimadoHoras;

    // Métricas reales (null hasta que se finalice)
    private Double costoFinal;
    private Double tiempoRealHoras;
}
