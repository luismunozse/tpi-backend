package ar.edu.utn.frc.backend.tpi.transportes.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.transportes.dto.RutaRequest;
import ar.edu.utn.frc.backend.tpi.transportes.dto.RutaResponse;
import ar.edu.utn.frc.backend.tpi.transportes.dto.TramoRequest;
import ar.edu.utn.frc.backend.tpi.transportes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Ruta;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Solicitud;
import ar.edu.utn.frc.backend.tpi.transportes.entity.TipoTramo;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Tramo;
import ar.edu.utn.frc.backend.tpi.transportes.mapper.RutaMapper;
import ar.edu.utn.frc.backend.tpi.transportes.mapper.TramoMapper;
import ar.edu.utn.frc.backend.tpi.transportes.repository.RutaRepository;
import ar.edu.utn.frc.backend.tpi.transportes.repository.SolicitudRepository;
import ar.edu.utn.frc.backend.tpi.transportes.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de rutas.
 * Implementa la creación de rutas con tramos y cálculo de métricas.
 *
 * Casos de uso relacionados:
 * - CU-03: Consultar rutas tentativas
 * - CU-04: Asignar ruta a solicitud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RutaService {

    private final RutaRepository rutaRepository;
    private final SolicitudRepository solicitudRepository;
    private final TramoRepository tramoRepository;

    /**
     * Crear una nueva ruta con sus tramos.
     * Los tramos se crean automáticamente a partir del request.
     *
     * @param request datos de la ruta y tramos
     * @return RutaResponse con los datos de la ruta creada
     */
    @Transactional
    public RutaResponse crearRuta(RutaRequest request) {
        log.info("Creando ruta con {} tramos",
                request.getTramos() != null ? request.getTramos().size() : 0);

        // Validar que tenga al menos un tramo
        if (request.getTramos() == null || request.getTramos().isEmpty()) {
            throw new IllegalArgumentException("La ruta debe tener al menos un tramo");
        }

        // Crear la ruta
        Ruta ruta = RutaMapper.toEntity(request);
        Ruta rutaGuardada = rutaRepository.save(ruta);

        // Crear los tramos asociados
        List<Tramo> tramos = new ArrayList<>();
        for (TramoRequest tramoRequest : request.getTramos()) {
            Tramo tramo = TramoMapper.toEntity(tramoRequest, rutaGuardada);
            tramos.add(tramo);
        }

        List<Tramo> tramosGuardados = tramoRepository.saveAll(tramos);
        rutaGuardada.setTramos(tramosGuardados);

        log.info("Ruta creada con id: {} y {} tramos", rutaGuardada.getId(), tramosGuardados.size());
        return RutaMapper.toResponse(rutaGuardada);
    }

    /**
     * Crear una ruta y asociarla a una solicitud.
     * Este método combina la creación de ruta con la asignación a la solicitud.
     *
     * @param solicitudId identificador de la solicitud
     * @param request datos de la ruta y tramos
     * @return RutaResponse con los datos de la ruta creada
     * @throws IllegalArgumentException si la solicitud no existe
     */
    @Transactional
    public RutaResponse crearRutaParaSolicitud(Long solicitudId, RutaRequest request) {
        log.info("Creando ruta para solicitud {}", solicitudId);

        // Validar que la solicitud existe
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud no encontrada con id: " + solicitudId));

        // Crear la ruta
        RutaResponse rutaResponse = crearRuta(request);

        // Asociar la ruta a la solicitud
        Ruta ruta = rutaRepository.findById(rutaResponse.getId())
                .orElseThrow(() -> new IllegalStateException("Error al crear la ruta"));

        solicitud.setRuta(ruta);
        solicitudRepository.save(solicitud);

        log.info("Ruta {} asociada a solicitud {}", ruta.getId(), solicitudId);
        return rutaResponse;
    }

    /**
     * Obtener una ruta por su ID.
     * Incluye todos los tramos asociados.
     *
     * @param id identificador de la ruta
     * @return RutaResponse con los datos completos de la ruta
     * @throws IllegalArgumentException si la ruta no existe
     */
    @Transactional(readOnly = true)
    public RutaResponse obtenerRutaPorId(Long id) {
        log.info("Consultando ruta con id: {}", id);

        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + id));

        return RutaMapper.toResponse(ruta);
    }

    /**
     * Obtener todas las rutas del sistema.
     *
     * @return lista de todas las rutas
     */
    @Transactional(readOnly = true)
    public List<RutaResponse> obtenerTodasLasRutas() {
        log.info("Consultando todas las rutas");

        return rutaRepository.findAll().stream()
                .map(RutaMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener rutas sin incluir los tramos.
     * Útil para listados donde no se necesita el detalle completo.
     *
     * @return lista de rutas sin tramos
     */
    @Transactional(readOnly = true)
    public List<RutaResponse> obtenerTodasLasRutasSinTramos() {
        log.info("Consultando todas las rutas (sin tramos)");

        return rutaRepository.findAll().stream()
                .map(RutaMapper::toResponseWithoutTramos)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar los datos generales de una ruta.
     * No modifica los tramos asociados.
     *
     * @param id identificador de la ruta
     * @param request nuevos datos de la ruta
     * @return RutaResponse con los datos actualizados
     * @throws IllegalArgumentException si la ruta no existe
     * @throws IllegalStateException si la ruta tiene tramos iniciados
     */
    @Transactional
    public RutaResponse actualizarRuta(Long id, RutaRequest request) {
        log.info("Actualizando ruta con id: {}", id);

        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + id));

        // No permitir actualizar si tiene tramos iniciados o finalizados
        boolean tieneTramoEnProceso = ruta.getTramos().stream()
                .anyMatch(t -> t.getEstado() == EstadoTramo.INICIADO
                        || t.getEstado() == EstadoTramo.FINALIZADO);

        if (tieneTramoEnProceso) {
            throw new IllegalStateException(
                    "No se puede actualizar una ruta con tramos en proceso o finalizados");
        }

        RutaMapper.updateEntity(ruta, request);
        Ruta rutaActualizada = rutaRepository.save(ruta);

        log.info("Ruta actualizada con id: {}", rutaActualizada.getId());
        return RutaMapper.toResponse(rutaActualizada);
    }

    /**
     * Eliminar una ruta del sistema.
     * Solo puede eliminarse si no está asociada a ninguna solicitud.
     *
     * @param id identificador de la ruta a eliminar
     * @throws IllegalArgumentException si la ruta no existe
     * @throws IllegalStateException si la ruta está asociada a una solicitud
     */
    @Transactional
    public void eliminarRuta(Long id) {
        log.info("Eliminando ruta con id: {}", id);

        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + id));

        // Validar que no esté asociada a una solicitud
        if (ruta.getSolicitud() != null) {
            throw new IllegalStateException(
                    "No se puede eliminar una ruta asociada a una solicitud");
        }

        rutaRepository.delete(ruta);
        log.info("Ruta eliminada con id: {}", id);
    }

    /**
     * Calcular métricas de la ruta basadas en los tramos.
     * Actualiza distancia total, tiempo estimado y costo estimado.
     *
     * @param id identificador de la ruta
     * @return RutaResponse con las métricas actualizadas
     * @throws IllegalArgumentException si la ruta no existe
     */
    @Transactional
    public RutaResponse recalcularMetricas(Long id) {
        log.info("Recalculando métricas de ruta {}", id);

        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + id));

        // Calcular costo estimado total (suma de costos de tramos)
        Double costoEstimado = ruta.getTramos().stream()
                .map(Tramo::getCostoEstimado)
                .filter(costo -> costo != null)
                .reduce(0.0, Double::sum);

        ruta.setCostoEstimado(costoEstimado);

        // Nota: La distancia total y tiempo estimado deberían calcularse
        // consultando el servicio de costos/geolocalización
        // Por ahora solo actualizamos el costo

        Ruta rutaActualizada = rutaRepository.save(ruta);
        log.info("Métricas recalculadas para ruta {}: costo={}", id, costoEstimado);

        return RutaMapper.toResponse(rutaActualizada);
    }

    /**
     * Obtener el progreso de una ruta.
     * Calcula cuántos tramos están completados.
     *
     * @param id identificador de la ruta
     * @return objeto con información de progreso
     * @throws IllegalArgumentException si la ruta no existe
     */
    @Transactional(readOnly = true)
    public ProgresoRuta obtenerProgresoRuta(Long id) {
        log.info("Consultando progreso de ruta {}", id);

        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + id));

        int totalTramos = ruta.getTramos().size();
        long tramosFinalizados = ruta.getTramos().stream()
                .filter(t -> t.getEstado() == EstadoTramo.FINALIZADO)
                .count();

        long tramosEnProceso = ruta.getTramos().stream()
                .filter(t -> t.getEstado() == EstadoTramo.INICIADO)
                .count();

        return ProgresoRuta.builder()
                .rutaId(id)
                .totalTramos(totalTramos)
                .tramosFinalizados((int) tramosFinalizados)
                .tramosEnProceso((int) tramosEnProceso)
                .porcentajeCompletado((totalTramos > 0) ? (tramosFinalizados * 100.0 / totalTramos) : 0.0)
                .build();
    }

    /**
     * Validar que una ruta es válida para asignarse a una solicitud.
     * Verifica tipos de tramos, secuencia, etc.
     *
     * @param rutaId identificador de la ruta
     * @return true si es válida, false si no
     */
    @Transactional(readOnly = true)
    public boolean esRutaValida(Long rutaId) {
        Ruta ruta = rutaRepository.findById(rutaId).orElse(null);
        if (ruta == null || ruta.getTramos().isEmpty()) {
            return false;
        }

        List<Tramo> tramos = ruta.getTramos();

        // El primer tramo debe ser ORIGEN_* (ORIGEN_DEPOSITO u ORIGEN_DESTINO)
        TipoTramo primerTipo = tramos.get(0).getTipo();
        if (primerTipo != TipoTramo.ORIGEN_DEPOSITO && primerTipo != TipoTramo.ORIGEN_DESTINO) {
            return false;
        }

        // Si solo hay un tramo, debe ser ORIGEN_DESTINO
        if (tramos.size() == 1) {
            return primerTipo == TipoTramo.ORIGEN_DESTINO;
        }

        // El último tramo debe terminar en destino
        TipoTramo ultimoTipo = tramos.get(tramos.size() - 1).getTipo();
        if (ultimoTipo != TipoTramo.DEPOSITO_DESTINO && ultimoTipo != TipoTramo.ORIGEN_DESTINO) {
            return false;
        }

        return true;
    }

    /**
     * DTO interno para representar el progreso de una ruta.
     */
    @lombok.Data
    @lombok.Builder
    public static class ProgresoRuta {
        private Long rutaId;
        private Integer totalTramos;
        private Integer tramosFinalizados;
        private Integer tramosEnProceso;
        private Double porcentajeCompletado;
    }
}
