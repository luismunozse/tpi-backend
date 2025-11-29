package ar.edu.utn.frc.backend.tpi.transportes.mapper;

import ar.edu.utn.frc.backend.tpi.transportes.dto.TramoRequest;
import ar.edu.utn.frc.backend.tpi.transportes.dto.TramoResponse;
import ar.edu.utn.frc.backend.tpi.transportes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Ruta;
import ar.edu.utn.frc.backend.tpi.transportes.entity.TipoTramo;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Tramo;

/**
 * Mapper para convertir entre entidades Tramo y DTOs.
 */
public class TramoMapper {

    private TramoMapper() {
        // Clase utilitaria, no debe instanciarse
    }

    /**
     * Convierte un TramoRequest a entidad Tramo.
     * Requiere la entidad Ruta para establecer la relación.
     *
     * @param request DTO de entrada
     * @param ruta entidad Ruta asociada
     * @return entidad Tramo
     */
    public static Tramo toEntity(TramoRequest request, Ruta ruta) {
        if (request == null) {
            return null;
        }

        return Tramo.builder()
                .ruta(ruta)
                .origen(request.getOrigen())
                .destino(request.getDestino())
                .tipo(TipoTramo.valueOf(request.getTipo()))
                .estado(EstadoTramo.ESTIMADO)
                .costoEstimado(request.getCostoEstimado())
                .build();
    }

    /**
     * Convierte una entidad Tramo a TramoResponse.
     *
     * @param tramo entidad
     * @return DTO de respuesta
     */
    public static TramoResponse toResponse(Tramo tramo) {
        if (tramo == null) {
            return null;
        }

        return TramoResponse.builder()
                .id(tramo.getId())
                .rutaId(tramo.getRuta() != null ? tramo.getRuta().getId() : null)
                .origen(tramo.getOrigen())
                .destino(tramo.getDestino())
                .tipo(tramo.getTipo() != null ? tramo.getTipo().name() : null)
                .estado(tramo.getEstado() != null ? tramo.getEstado().name() : null)
                .costoEstimado(tramo.getCostoEstimado())
                .costoReal(tramo.getCostoReal())
                .fechaHoraInicio(tramo.getFechaHoraInicio())
                .fechaHoraFin(tramo.getFechaHoraFin())
                .camionId(tramo.getCamionId())
                .build();
    }

    /**
     * Actualiza una entidad Tramo existente con datos de un TramoRequest.
     * No modifica el ID, estado, ni la ruta asociada.
     *
     * @param tramo entidad existente
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Tramo tramo, TramoRequest request) {
        if (tramo == null || request == null) {
            return;
        }

        tramo.setOrigen(request.getOrigen());
        tramo.setDestino(request.getDestino());
        tramo.setTipo(TipoTramo.valueOf(request.getTipo()));
        tramo.setCostoEstimado(request.getCostoEstimado());
    }
}
