package ar.edu.utn.frc.backend.tpi.flota.mapper;

import ar.edu.utn.frc.backend.tpi.flota.dto.DepositoDto;
import ar.edu.utn.frc.backend.tpi.flota.model.Deposito;

public class DepositoMapper {

    private DepositoMapper() {}

    public static DepositoDto toDto(Deposito entity) {
        if (entity == null) return null;
        return DepositoDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .latitud(entity.getLatitud())
                .longitud(entity.getLongitud())
                .costoEstadiaDiaria(entity.getCostoEstadiaDiaria())
                .build();
    }

    public static Deposito toEntity(DepositoDto dto) {
        if (dto == null) return null;
        return Deposito.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .costoEstadiaDiaria(dto.getCostoEstadiaDiaria())
                .build();
    }
}

