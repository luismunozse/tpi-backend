package ar.edu.utn.frc.backend.tpi.solicitudes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de solicitudes de transporte.
 * Usado en CU-01: Registrar solicitud de transporte.
 * Soporta creación atómica de cliente y contenedor (RF 1.1, RF 1.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequest {

    @NotNull(message = "Los datos del cliente son obligatorios")
    @Valid
    private ClienteRequest cliente;

    @NotNull(message = "Los datos del contenedor son obligatorios")
    @Valid
    private ContenedorRequest contenedor;

    @NotBlank(message = "La dirección de origen es obligatoria")
    private String origenDireccion;

    @NotNull(message = "La latitud de origen es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double origenLatitud;

    @NotNull(message = "La longitud de origen es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double origenLongitud;

    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinoDireccion;

    @NotNull(message = "La latitud de destino es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double destinoLatitud;

    @NotNull(message = "La longitud de destino es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double destinoLongitud;
}
