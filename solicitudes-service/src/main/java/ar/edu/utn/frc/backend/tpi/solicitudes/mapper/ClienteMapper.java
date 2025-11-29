package ar.edu.utn.frc.backend.tpi.solicitudes.mapper;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ClienteRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ClienteResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Cliente;

/**
 * Mapper para convertir entre entidades Cliente y DTOs.
 * Utiliza métodos estáticos para facilitar la conversión.
 */
public class ClienteMapper {

    private ClienteMapper() {
        // Clase utilitaria, no debe instanciarse
    }

    /**
     * Convierte un ClienteRequest a entidad Cliente.
     *
     * @param request DTO de entrada
     * @return entidad Cliente
     */
    public static Cliente toEntity(ClienteRequest request) {
        if (request == null) {
            return null;
        }

        return Cliente.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .build();
    }

    /**
     * Convierte una entidad Cliente a ClienteResponse.
     *
     * @param cliente entidad
     * @return DTO de respuesta
     */
    public static ClienteResponse toResponse(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .build();
    }

    /**
     * Actualiza una entidad Cliente existente con datos de un ClienteRequest.
     * No modifica el ID.
     *
     * @param cliente entidad existente
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Cliente cliente, ClienteRequest request) {
        if (cliente == null || request == null) {
            return;
        }

        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
    }
}
