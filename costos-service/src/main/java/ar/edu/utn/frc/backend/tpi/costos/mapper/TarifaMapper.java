package ar.edu.utn.frc.backend.tpi.costos.mapper;

import ar.edu.utn.frc.backend.tpi.costos.dto.TarifaDto;
import ar.edu.utn.frc.backend.tpi.costos.model.Tarifa;

public class TarifaMapper {

    private TarifaMapper() {}

    public static TarifaDto toDto(Tarifa entity) {
        if (entity == null) return null;
        return TarifaDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .costoBaseKm(entity.getCostoBaseKm())
                .valorCombustibleLitro(entity.getValorCombustibleLitro())
                .costoEstadiaDiaria(entity.getCostoEstadiaDiaria())
                .velocidadPromedioKmH(entity.getVelocidadPromedioKmH())
                .costoGestionFijo(entity.getCostoGestionFijo())
                .build();
    }

    public static Tarifa toEntity(TarifaDto dto) {
        if (dto == null) return null;
        return Tarifa.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .costoBaseKm(dto.getCostoBaseKm())
                .valorCombustibleLitro(dto.getValorCombustibleLitro())
                .costoEstadiaDiaria(dto.getCostoEstadiaDiaria())
                .velocidadPromedioKmH(dto.getVelocidadPromedioKmH())
                .costoGestionFijo(dto.getCostoGestionFijo())
                .build();
    }
}
