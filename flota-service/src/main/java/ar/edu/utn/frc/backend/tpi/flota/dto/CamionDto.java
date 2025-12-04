package ar.edu.utn.frc.backend.tpi.flota.dto;

import ar.edu.utn.frc.backend.tpi.flota.model.EstadoCamion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CamionDto {
    private Long id;

    @NotBlank(message = "El dominio es obligatorio")
    private String dominio;

    // Relación con Transportista
    @NotNull(message = "El transportista es obligatorio")
    private Long transportistaId;

    // Información del transportista (solo para lectura)
    private String nombreTransportista;
    private String apellidoTransportista;
    private String telefonoTransportista;

    @NotNull(message = "La capacidad de peso es obligatoria")
    private Double capacidadPesoKg;

    @NotNull(message = "La capacidad de volumen es obligatoria")
    private Double capacidadVolumenM3;

    @NotNull(message = "El consumo de combustible es obligatorio")
    private Double consumoCombustibleLitrosKm;

    @NotNull(message = "El costo base por km es obligatorio")
    private Double costoBaseKm;

    private EstadoCamion estado;
}
