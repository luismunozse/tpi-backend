package ar.edu.utn.frc.backend.tpi.solicitudes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.TramoRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.TramoResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Ruta;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Tramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.mapper.TramoMapper;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.RutaRepository;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de tramos.
 * Implementa operaciones CRUD y gestión de estados de tramos.
 *
 * Casos de uso relacionados:
 * - CU-06: Asignar camión a tramo
 * - CU-07: Registrar inicio de tramo
 * - CU-08: Registrar fin de tramo
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;

    /**
     * Crear un nuevo tramo asociado a una ruta.
     *
     * @param request datos del tramo
     * @return TramoResponse con los datos del tramo creado
     * @throws IllegalArgumentException si la ruta no existe
     */
    @Transactional
    public TramoResponse crearTramo(TramoRequest request) {
        log.info("Creando tramo para ruta {}", request.getRutaId());

        // Validar que la ruta existe
        Ruta ruta = rutaRepository.findById(request.getRutaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + request.getRutaId()));

        Tramo tramo = TramoMapper.toEntity(request, ruta);
        Tramo tramoGuardado = tramoRepository.save(tramo);

        log.info("Tramo creado con id: {}", tramoGuardado.getId());
        return TramoMapper.toResponse(tramoGuardado);
    }

    /**
     * Obtener un tramo por su ID.
     *
     * @param id identificador del tramo
     * @return TramoResponse con los datos del tramo
     * @throws IllegalArgumentException si el tramo no existe
     */
    @Transactional(readOnly = true)
    public TramoResponse obtenerTramoPorId(Long id) {
        log.info("Consultando tramo con id: {}", id);

        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + id));

        return TramoMapper.toResponse(tramo);
    }

    /**
     * Obtener todos los tramos de una ruta.
     *
     * @param rutaId identificador de la ruta
     * @return lista de tramos de la ruta
     * @throws IllegalArgumentException si la ruta no existe
     */
    @Transactional(readOnly = true)
    public List<TramoResponse> obtenerTramosPorRuta(Long rutaId) {
        log.info("Consultando tramos de la ruta {}", rutaId);

        // Validar que la ruta existe
        if (!rutaRepository.existsById(rutaId)) {
            throw new IllegalArgumentException("Ruta no encontrada con id: " + rutaId);
        }

        return tramoRepository.findByRutaId(rutaId).stream()
                .map(TramoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener tramos por estado.
     *
     * @param estado estado del tramo
     * @return lista de tramos con el estado especificado
     */
    @Transactional(readOnly = true)
    public List<TramoResponse> obtenerTramosPorEstado(EstadoTramo estado) {
        log.info("Consultando tramos con estado: {}", estado);

        return tramoRepository.findByEstado(estado).stream()
                .map(TramoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener tramos asignados a un camión específico.
     * Útil para que los transportistas vean sus asignaciones (CU-07).
     *
     * @param camionId identificador del camión
     * @return lista de tramos asignados al camión
     */
    @Transactional(readOnly = true)
    public List<TramoResponse> obtenerTramosPorCamion(Long camionId) {
        log.info("Consultando tramos del camión {}", camionId);

        return tramoRepository.findByCamionId(camionId).stream()
                .map(TramoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los tramos del sistema.
     *
     * @return lista de todos los tramos
     */
    @Transactional(readOnly = true)
    public List<TramoResponse> obtenerTodosLosTramos() {
        log.info("Consultando todos los tramos");

        return tramoRepository.findAll().stream()
                .map(TramoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar datos de un tramo.
     * No permite modificar si el tramo ya fue iniciado.
     *
     * @param id identificador del tramo
     * @param request nuevos datos del tramo
     * @return TramoResponse con los datos actualizados
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo ya fue iniciado
     */
    @Transactional
    public TramoResponse actualizarTramo(Long id, TramoRequest request) {
        log.info("Actualizando tramo con id: {}", id);

        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + id));

        // No permitir actualizar si ya fue iniciado o finalizado
        if (tramo.getEstado() == EstadoTramo.INICIADO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
            throw new IllegalStateException(
                    "No se puede actualizar un tramo en estado: " + tramo.getEstado());
        }

        TramoMapper.updateEntity(tramo, request);
        Tramo tramoActualizado = tramoRepository.save(tramo);

        log.info("Tramo actualizado con id: {}", tramoActualizado.getId());
        return TramoMapper.toResponse(tramoActualizado);
    }

    /**
     * Asignar un camión a un tramo.
     * Implementa CU-06: Asignar camión a tramo.
     *
     * @param tramoId identificador del tramo
     * @param camionId identificador del camión
     * @return TramoResponse con el camión asignado
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no está en estado ESTIMADO
     */
    @Transactional
    public TramoResponse asignarCamion(Long tramoId, Long camionId) {
        log.info("Asignando camión {} al tramo {}", camionId, tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        // Solo se puede asignar camión si está en estado ESTIMADO
        if (tramo.getEstado() != EstadoTramo.ESTIMADO) {
            throw new IllegalStateException(
                    "Solo se puede asignar camión a tramos en estado ESTIMADO. Estado actual: "
                            + tramo.getEstado());
        }

        // TODO: Validar que el camión existe y está disponible
        // Esto se hará cuando se integre con el microservicio de Flota

        tramo.setCamionId(camionId);
        tramo.setEstado(EstadoTramo.ASIGNADO);

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Camión {} asignado al tramo {}", camionId, tramoId);

        return TramoMapper.toResponse(tramoActualizado);
    }

    /**
     * Iniciar un tramo.
     * Implementa CU-07: Registrar inicio de tramo.
     * Marca la fecha y hora de inicio y cambia el estado a INICIADO.
     *
     * @param tramoId identificador del tramo
     * @return TramoResponse con el tramo iniciado
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no está en estado ASIGNADO
     */
    @Transactional
    public TramoResponse iniciarTramo(Long tramoId) {
        log.info("Iniciando tramo {}", tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        // Solo se puede iniciar si está ASIGNADO
        if (tramo.getEstado() != EstadoTramo.ASIGNADO) {
            throw new IllegalStateException(
                    "Solo se puede iniciar tramos en estado ASIGNADO. Estado actual: "
                            + tramo.getEstado());
        }

        // Validar que tiene camión asignado
        if (tramo.getCamionId() == null) {
            throw new IllegalStateException(
                    "No se puede iniciar un tramo sin camión asignado");
        }

        tramo.setFechaHoraInicio(LocalDateTime.now());
        tramo.setEstado(EstadoTramo.INICIADO);

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo {} iniciado en {}", tramoId, tramoActualizado.getFechaHoraInicio());

        return TramoMapper.toResponse(tramoActualizado);
    }

    /**
     * Finalizar un tramo.
     * Implementa CU-08: Registrar fin de tramo.
     * Marca la fecha y hora de fin y cambia el estado a FINALIZADO.
     *
     * @param tramoId identificador del tramo
     * @return TramoResponse con el tramo finalizado
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no está en estado INICIADO
     */
    @Transactional
    public TramoResponse finalizarTramo(Long tramoId) {
        log.info("Finalizando tramo {}", tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        // Solo se puede finalizar si está INICIADO
        if (tramo.getEstado() != EstadoTramo.INICIADO) {
            throw new IllegalStateException(
                    "Solo se puede finalizar tramos en estado INICIADO. Estado actual: "
                            + tramo.getEstado());
        }

        tramo.setFechaHoraFin(LocalDateTime.now());
        tramo.setEstado(EstadoTramo.FINALIZADO);

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo {} finalizado en {}", tramoId, tramoActualizado.getFechaHoraFin());

        return TramoMapper.toResponse(tramoActualizado);
    }

    /**
     * Actualizar el costo real de un tramo.
     * Se llama después de finalizar el tramo para registrar el costo real calculado.
     *
     * @param tramoId identificador del tramo
     * @param costoReal costo real del tramo
     * @return TramoResponse con el costo actualizado
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no está finalizado
     */
    @Transactional
    public TramoResponse actualizarCostoReal(Long tramoId, Double costoReal) {
        log.info("Actualizando costo real del tramo {} a {}", tramoId, costoReal);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        // Solo se puede actualizar costo real de tramos finalizados
        if (tramo.getEstado() != EstadoTramo.FINALIZADO) {
            throw new IllegalStateException(
                    "Solo se puede actualizar el costo real de tramos finalizados");
        }

        tramo.setCostoReal(costoReal);
        Tramo tramoActualizado = tramoRepository.save(tramo);

        log.info("Costo real del tramo {} actualizado", tramoId);
        return TramoMapper.toResponse(tramoActualizado);
    }

    /**
     * Eliminar un tramo del sistema.
     * Solo puede eliminarse si está en estado ESTIMADO.
     *
     * @param id identificador del tramo a eliminar
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no puede ser eliminado
     */
    @Transactional
    public void eliminarTramo(Long id) {
        log.info("Eliminando tramo con id: {}", id);

        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + id));

        // Solo permitir eliminar si está en estado ESTIMADO
        if (tramo.getEstado() != EstadoTramo.ESTIMADO) {
            throw new IllegalStateException(
                    "Solo se pueden eliminar tramos en estado ESTIMADO. Estado actual: "
                            + tramo.getEstado());
        }

        tramoRepository.delete(tramo);
        log.info("Tramo eliminado con id: {}", id);
    }

    /**
     * Cambiar el estado de un tramo.
     * Para casos especiales que requieran cambios de estado manuales.
     *
     * @param tramoId identificador del tramo
     * @param nuevoEstado nuevo estado
     * @return TramoResponse con el estado actualizado
     * @throws IllegalArgumentException si el tramo no existe
     */
    @Transactional
    public TramoResponse cambiarEstado(Long tramoId, EstadoTramo nuevoEstado) {
        log.info("Cambiando estado del tramo {} a {}", tramoId, nuevoEstado);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        tramo.setEstado(nuevoEstado);
        Tramo tramoActualizado = tramoRepository.save(tramo);

        log.info("Estado del tramo {} actualizado a {}", tramoId, nuevoEstado);
        return TramoMapper.toResponse(tramoActualizado);
    }
}
