package ar.edu.utn.frc.backend.tpi.solicitudes.mapper;

import java.time.LocalDateTime;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.RutaResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Cliente;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoSolicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Solicitud;

/**
 * Mapper para convertir entre entidades Solicitud y DTOs.
 */
public class SolicitudMapper {

    private SolicitudMapper() {
        // Clase utilitaria, no debe instanciarse
    }

    /**
     * Convierte un SolicitudRequest a entidad Solicitud.
     * Requiere las entidades Cliente y Contenedor para establecer las relaciones.
     *
     * @param request DTO de entrada
     * @param cliente entidad Cliente asociada
     * @param contenedor entidad Contenedor asociada
     * @return entidad Solicitud
     */
    public static Solicitud toEntity(SolicitudRequest request, Cliente cliente, Contenedor contenedor) {
        if (request == null) {
            return null;
        }

        return Solicitud.builder()
                .cliente(cliente)
                .contenedor(contenedor)
                .origenDireccion(request.getOrigenDireccion())
                .origenLatitud(request.getOrigenLatitud())
                .origenLongitud(request.getOrigenLongitud())
                .destinoDireccion(request.getDestinoDireccion())
                .destinoLatitud(request.getDestinoLatitud())
                .destinoLongitud(request.getDestinoLongitud())
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    /**
     * Convierte una entidad Solicitud a SolicitudResponse completo.
     * Incluye todos los detalles del cliente, contenedor y ruta.
     *
     * @param solicitud entidad
     * @return DTO de respuesta completo
     */
    public static SolicitudResponse toResponse(Solicitud solicitud) {
        if (solicitud == null) {
            return null;
        }

        // Convertir ruta a response si existe
        RutaResponse rutaResponse = null;
        if (solicitud.getRuta() != null) {
            rutaResponse = RutaMapper.toResponse(solicitud.getRuta());
        }

        return SolicitudResponse.builder()
                // Datos básicos
                .id(solicitud.getId())
                .estado(solicitud.getEstado() != null ? solicitud.getEstado().name() : null)
                .fechaCreacion(solicitud.getFechaCreacion())

                // Información del cliente
                .clienteId(solicitud.getCliente() != null ? solicitud.getCliente().getId() : null)
                .clienteNombre(solicitud.getCliente() != null ? solicitud.getCliente().getNombre() : null)
                .clienteEmail(solicitud.getCliente() != null ? solicitud.getCliente().getEmail() : null)

                // Información del contenedor
                .contenedorId(solicitud.getContenedor() != null ? solicitud.getContenedor().getId() : null)
                .contenedorNumeroSerie(
                        solicitud.getContenedor() != null ? solicitud.getContenedor().getNumeroSerie() : null)
                .contenedorPeso(solicitud.getContenedor() != null ? solicitud.getContenedor().getPeso() : null)
                .contenedorVolumen(solicitud.getContenedor() != null ? solicitud.getContenedor().getVolumen() : null)

                // Ubicaciones
                .origenDireccion(solicitud.getOrigenDireccion())
                .origenLatitud(solicitud.getOrigenLatitud())
                .origenLongitud(solicitud.getOrigenLongitud())
                .destinoDireccion(solicitud.getDestinoDireccion())
                .destinoLatitud(solicitud.getDestinoLatitud())
                .destinoLongitud(solicitud.getDestinoLongitud())

                // Ruta
                .rutaId(solicitud.getRuta() != null ? solicitud.getRuta().getId() : null)
                .ruta(rutaResponse)

                // Métricas estimadas
                .costoEstimado(solicitud.getCostoEstimado())
                .tiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras())

                // Métricas reales
                .costoFinal(solicitud.getCostoFinal())
                .tiempoRealHoras(solicitud.getTiempoRealHoras())

                .build();
    }

    /**
     * Convierte una entidad Solicitud a SolicitudResponse sin incluir la ruta completa.
     * Útil para listados donde no se necesitan todos los detalles de la ruta.
     *
     * @param solicitud entidad
     * @return DTO de respuesta simplificado
     */
    public static SolicitudResponse toResponseWithoutRuta(Solicitud solicitud) {
        if (solicitud == null) {
            return null;
        }

        return SolicitudResponse.builder()
                // Datos básicos
                .id(solicitud.getId())
                .estado(solicitud.getEstado() != null ? solicitud.getEstado().name() : null)
                .fechaCreacion(solicitud.getFechaCreacion())

                // Información del cliente
                .clienteId(solicitud.getCliente() != null ? solicitud.getCliente().getId() : null)
                .clienteNombre(solicitud.getCliente() != null ? solicitud.getCliente().getNombre() : null)
                .clienteEmail(solicitud.getCliente() != null ? solicitud.getCliente().getEmail() : null)

                // Información del contenedor
                .contenedorId(solicitud.getContenedor() != null ? solicitud.getContenedor().getId() : null)
                .contenedorNumeroSerie(
                        solicitud.getContenedor() != null ? solicitud.getContenedor().getNumeroSerie() : null)
                .contenedorPeso(solicitud.getContenedor() != null ? solicitud.getContenedor().getPeso() : null)
                .contenedorVolumen(solicitud.getContenedor() != null ? solicitud.getContenedor().getVolumen() : null)

                // Ubicaciones
                .origenDireccion(solicitud.getOrigenDireccion())
                .origenLatitud(solicitud.getOrigenLatitud())
                .origenLongitud(solicitud.getOrigenLongitud())
                .destinoDireccion(solicitud.getDestinoDireccion())
                .destinoLatitud(solicitud.getDestinoLatitud())
                .destinoLongitud(solicitud.getDestinoLongitud())

                // Solo ID de la ruta
                .rutaId(solicitud.getRuta() != null ? solicitud.getRuta().getId() : null)

                // Métricas
                .costoEstimado(solicitud.getCostoEstimado())
                .tiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras())
                .costoFinal(solicitud.getCostoFinal())
                .tiempoRealHoras(solicitud.getTiempoRealHoras())

                .build();
    }

    /**
     * Actualiza una entidad Solicitud existente con datos de un SolicitudRequest.
     * No modifica el ID, cliente, contenedor, estado ni fechas.
     *
     * @param solicitud entidad existente
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Solicitud solicitud, SolicitudRequest request) {
        if (solicitud == null || request == null) {
            return;
        }

        solicitud.setOrigenDireccion(request.getOrigenDireccion());
        solicitud.setOrigenLatitud(request.getOrigenLatitud());
        solicitud.setOrigenLongitud(request.getOrigenLongitud());
        solicitud.setDestinoDireccion(request.getDestinoDireccion());
        solicitud.setDestinoLatitud(request.getDestinoLatitud());
        solicitud.setDestinoLongitud(request.getDestinoLongitud());
    }
}
