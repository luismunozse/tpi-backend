package ar.edu.utn.frc.backend.tpi.flota.mapper;

import ar.edu.utn.frc.backend.tpi.flota.dto.TransportistaDto;
import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;

public final class TransportistaMapper {

    private TransportistaMapper() {
    }

    public static TransportistaDto toDto(Transportista entity) {
        return TransportistaDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .dni(entity.getDni())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .numeroLicencia(entity.getNumeroLicencia())
                .categoriaLicencia(entity.getCategoriaLicencia())
                .activo(entity.isActivo())
                .build();
    }

    public static Transportista toEntity(TransportistaDto dto) {
        return Transportista.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .numeroLicencia(dto.getNumeroLicencia())
                .categoriaLicencia(dto.getCategoriaLicencia())
                .activo(Boolean.TRUE.equals(dto.getActivo()))
                .build();
    }
}
