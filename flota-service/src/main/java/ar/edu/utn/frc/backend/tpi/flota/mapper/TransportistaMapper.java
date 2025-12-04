package ar.edu.utn.frc.backend.tpi.flota.mapper;

import ar.edu.utn.frc.backend.tpi.flota.dto.TransportistaDto;
import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;

/**
 * Mapper para convertir entre entidad Transportista y TransportistaDto.
 */
public class TransportistaMapper {

    public static TransportistaDto toDto(Transportista transportista) {
        if (transportista == null) {
            return null;
        }

        return TransportistaDto.builder()
                .id(transportista.getId())
                .nombre(transportista.getNombre())
                .apellido(transportista.getApellido())
                .dni(transportista.getDni())
                .email(transportista.getEmail())
                .telefono(transportista.getTelefono())
                .numeroLicencia(transportista.getNumeroLicencia())
                .categoriaLicencia(transportista.getCategoriaLicencia())
                .activo(transportista.getActivo())
                .cantidadCamionesAsignados(transportista.getCamiones() != null ?
                        transportista.getCamiones().size() : 0)
                .build();
    }

    public static Transportista toEntity(TransportistaDto dto) {
        if (dto == null) {
            return null;
        }

        return Transportista.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .numeroLicencia(dto.getNumeroLicencia())
                .categoriaLicencia(dto.getCategoriaLicencia())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }

    public static void updateEntity(Transportista transportista, TransportistaDto dto) {
        if (transportista == null || dto == null) {
            return;
        }

        transportista.setNombre(dto.getNombre());
        transportista.setApellido(dto.getApellido());
        transportista.setDni(dto.getDni());
        transportista.setEmail(dto.getEmail());
        transportista.setTelefono(dto.getTelefono());
        transportista.setNumeroLicencia(dto.getNumeroLicencia());
        transportista.setCategoriaLicencia(dto.getCategoriaLicencia());
        if (dto.getActivo() != null) {
            transportista.setActivo(dto.getActivo());
        }
    }
}
