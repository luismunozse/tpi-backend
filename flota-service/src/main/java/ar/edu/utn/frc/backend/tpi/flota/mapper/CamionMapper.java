package ar.edu.utn.frc.backend.tpi.flota.mapper;

import ar.edu.utn.frc.backend.tpi.flota.dto.CamionDto;
import ar.edu.utn.frc.backend.tpi.flota.model.Camion;
import ar.edu.utn.frc.backend.tpi.flota.model.EstadoCamion;
import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;

public class CamionMapper {

    private CamionMapper() {}

    public static CamionDto toDto(Camion entity) {
        if (entity == null) return null;

        CamionDto.CamionDtoBuilder builder = CamionDto.builder()
                .id(entity.getId())
                .dominio(entity.getDominio())
                .capacidadPesoKg(entity.getCapacidadPesoKg())
                .capacidadVolumenM3(entity.getCapacidadVolumenM3())
                .consumoCombustibleLitrosKm(entity.getConsumoCombustibleLitrosKm())
                .costoBaseKm(entity.getCostoBaseKm())
                .estado(entity.getEstado());

        // Mapear información del transportista si existe
        if (entity.getTransportista() != null) {
            Transportista t = entity.getTransportista();
            builder.transportistaId(t.getId())
                   .nombreTransportista(t.getNombre())
                   .apellidoTransportista(t.getApellido())
                   .telefonoTransportista(t.getTelefono());
        }

        return builder.build();
    }

    public static Camion toEntity(CamionDto dto, Transportista transportista) {
        if (dto == null) return null;
        return Camion.builder()
                .id(dto.getId())
                .dominio(dto.getDominio())
                .transportista(transportista)
                .capacidadPesoKg(dto.getCapacidadPesoKg())
                .capacidadVolumenM3(dto.getCapacidadVolumenM3())
                .consumoCombustibleLitrosKm(dto.getConsumoCombustibleLitrosKm())
                .costoBaseKm(dto.getCostoBaseKm())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoCamion.DISPONIBLE)
                .build();
    }

    public static void updateEntity(Camion camion, CamionDto dto, Transportista transportista) {
        if (camion == null || dto == null) return;

        camion.setDominio(dto.getDominio());
        camion.setTransportista(transportista);
        camion.setCapacidadPesoKg(dto.getCapacidadPesoKg());
        camion.setCapacidadVolumenM3(dto.getCapacidadVolumenM3());
        camion.setConsumoCombustibleLitrosKm(dto.getConsumoCombustibleLitrosKm());
        camion.setCostoBaseKm(dto.getCostoBaseKm());
        if (dto.getEstado() != null) {
            camion.setEstado(dto.getEstado());
        }
    }
}
