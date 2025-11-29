package ar.edu.utn.frc.backend.tpi.solicitudes.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.RutaRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.RutaResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.TramoResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Ruta;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.TipoTramo;

/**
 * Mapper para convertir entre entidades Ruta y DTOs.
 */
public class RutaMapper {

    private RutaMapper() {
        // Clase utilitaria, no debe instanciarse
    }

    /**
     * Convierte un RutaRequest a entidad Ruta.
     * Los tramos se deben agregar posteriormente.
     *
     * @param request DTO de entrada
     * @return entidad Ruta
     */
    public static Ruta toEntity(RutaRequest request) {
        if (request == null) {
            return null;
        }

        return Ruta.builder()
                .distanciaTotalKm(request.getDistanciaTotalKm())
                .tiempoEstimadoHoras(request.getTiempoEstimadoHoras())
                .costoEstimado(request.getCostoEstimado())
                .build();
    }

    /**
     * Convierte una entidad Ruta a RutaResponse.
     * Incluye todos los tramos asociados.
     *
     * @param ruta entidad
     * @return DTO de respuesta
     */
    public static RutaResponse toResponse(Ruta ruta) {
        if (ruta == null) {
            return null;
        }

        List<TramoResponse> tramosResponse = ruta.getTramos() != null
                ? ruta.getTramos().stream()
                        .map(TramoMapper::toResponse)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        // Calcular cantidad de depósitos basado en tipos de tramo
        int cantidadDepositos = ruta.getTramos() != null
                ? (int) ruta.getTramos().stream()
                        .map(tramo -> tramo.getTipo())
                        .filter(tipo -> tipo == TipoTramo.DEPOSITO_DEPOSITO
                                || tipo == TipoTramo.ORIGEN_DEPOSITO
                                || tipo == TipoTramo.DEPOSITO_DESTINO)
                        .count()
                : 0;

        return RutaResponse.builder()
                .id(ruta.getId())
                .solicitudId(ruta.getSolicitud() != null ? ruta.getSolicitud().getId() : null)
                .distanciaTotalKm(ruta.getDistanciaTotalKm())
                .tiempoEstimadoHoras(ruta.getTiempoEstimadoHoras())
                .costoEstimado(ruta.getCostoEstimado())
                .tramos(tramosResponse)
                .cantidadTramos(tramosResponse.size())
                .cantidadDepositos(cantidadDepositos)
                .build();
    }

    /**
     * Convierte una entidad Ruta a RutaResponse sin incluir los tramos.
     * Útil para evitar carga innecesaria de datos en listados.
     *
     * @param ruta entidad
     * @return DTO de respuesta sin tramos
     */
    public static RutaResponse toResponseWithoutTramos(Ruta ruta) {
        if (ruta == null) {
            return null;
        }

        int cantidadTramos = ruta.getTramos() != null ? ruta.getTramos().size() : 0;

        return RutaResponse.builder()
                .id(ruta.getId())
                .solicitudId(ruta.getSolicitud() != null ? ruta.getSolicitud().getId() : null)
                .distanciaTotalKm(ruta.getDistanciaTotalKm())
                .tiempoEstimadoHoras(ruta.getTiempoEstimadoHoras())
                .costoEstimado(ruta.getCostoEstimado())
                .cantidadTramos(cantidadTramos)
                .build();
    }

    /**
     * Actualiza una entidad Ruta existente con datos de un RutaRequest.
     * No modifica el ID ni la solicitud asociada.
     *
     * @param ruta entidad existente
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Ruta ruta, RutaRequest request) {
        if (ruta == null || request == null) {
            return;
        }

        ruta.setDistanciaTotalKm(request.getDistanciaTotalKm());
        ruta.setTiempoEstimadoHoras(request.getTiempoEstimadoHoras());
        ruta.setCostoEstimado(request.getCostoEstimado());
    }
}
