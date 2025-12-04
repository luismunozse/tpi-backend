package ar.edu.utn.frc.backend.tpi.flota.mapper;

import ar.edu.utn.frc.backend.tpi.flota.dto.CamionDto;
import ar.edu.utn.frc.backend.tpi.flota.model.Camion;
import ar.edu.utn.frc.backend.tpi.flota.model.EstadoCamion;

public class CamionMapper {

    private CamionMapper() {}

    public static CamionDto toDto(Camion entity) {
        if (entity == null) return null;
        return CamionDto.builder()
                .id(entity.getId())
                .dominio(entity.getDominio())
                .nombreTransportista(entity.getNombreTransportista())
                .telefono(entity.getTelefono())
                .capacidadPesoKg(entity.getCapacidadPesoKg())
                .capacidadVolumenM3(entity.getCapacidadVolumenM3())
                .consumoCombustibleLitrosKm(entity.getConsumoCombustibleLitrosKm())
                .costoBaseKm(entity.getCostoBaseKm())
                .estado(entity.getEstado())
                .build();
    }

    public static Camion toEntity(CamionDto dto) {
        if (dto == null) return null;
        return Camion.builder()
                .id(dto.getId())
                .dominio(dto.getDominio())
                .nombreTransportista(dto.getNombreTransportista())
                .telefono(dto.getTelefono())
                .capacidadPesoKg(dto.getCapacidadPesoKg())
                .capacidadVolumenM3(dto.getCapacidadVolumenM3())
                .consumoCombustibleLitrosKm(dto.getConsumoCombustibleLitrosKm())
                .costoBaseKm(dto.getCostoBaseKm())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoCamion.DISPONIBLE)
                .build();
    }
}
