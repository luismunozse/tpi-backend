package ar.edu.utn.frc.backend.tpi.transportes.mapper;

import ar.edu.utn.frc.backend.tpi.transportes.dto.ContenedorRequest;
import ar.edu.utn.frc.backend.tpi.transportes.dto.ContenedorResponse;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Cliente;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.transportes.entity.EstadoContenedor;

/**
 * Mapper para convertir entre entidades Contenedor y DTOs.
 */
public class ContenedorMapper {

    private ContenedorMapper() {
        // Clase utilitaria, no debe instanciarse
    }

    /**
     * Convierte un ContenedorRequest a entidad Contenedor.
     * Requiere la entidad Cliente para establecer la relación.
     *
     * @param request DTO de entrada
     * @param cliente entidad Cliente asociada
     * @return entidad Contenedor
     */
    public static Contenedor toEntity(ContenedorRequest request, Cliente cliente) {
        if (request == null) {
            return null;
        }

        return Contenedor.builder()
                .numeroSerie(request.getNumeroSerie())
                .tipo(request.getTipo())
                .peso(request.getPeso())
                .volumen(request.getVolumen())
                .estado(EstadoContenedor.REGISTRADO)
                .cliente(cliente)
                .build();
    }

    /**
     * Convierte una entidad Contenedor a ContenedorResponse.
     *
     * @param contenedor entidad
     * @return DTO de respuesta
     */
    public static ContenedorResponse toResponse(Contenedor contenedor) {
        if (contenedor == null) {
            return null;
        }

        return ContenedorResponse.builder()
                .id(contenedor.getId())
                .numeroSerie(contenedor.getNumeroSerie())
                .tipo(contenedor.getTipo())
                .peso(contenedor.getPeso())
                .volumen(contenedor.getVolumen())
                .estado(contenedor.getEstado() != null ? contenedor.getEstado().name() : null)
                .clienteId(contenedor.getCliente() != null ? contenedor.getCliente().getId() : null)
                .clienteNombre(contenedor.getCliente() != null ? contenedor.getCliente().getNombre() : null)
                .build();
    }

    /**
     * Actualiza una entidad Contenedor existente con datos de un ContenedorRequest.
     * No modifica el ID, estado ni el cliente asociado.
     *
     * @param contenedor entidad existente
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Contenedor contenedor, ContenedorRequest request) {
        if (contenedor == null || request == null) {
            return;
        }

        contenedor.setNumeroSerie(request.getNumeroSerie());
        contenedor.setTipo(request.getTipo());
        contenedor.setPeso(request.getPeso());
        contenedor.setVolumen(request.getVolumen());
    }
}
