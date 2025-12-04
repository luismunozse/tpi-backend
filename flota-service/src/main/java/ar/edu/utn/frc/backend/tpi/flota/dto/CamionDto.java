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

    @NotBlank
    private String dominio;

    @NotBlank
    private String nombreTransportista;

    @NotBlank
    private String telefono;

    @NotNull
    private Double capacidadPesoKg;

    @NotNull
    private Double capacidadVolumenM3;

    @NotNull
    private Double consumoCombustibleLitrosKm;

    @NotNull
    private Double costoBaseKm;

    private EstadoCamion estado;
}
