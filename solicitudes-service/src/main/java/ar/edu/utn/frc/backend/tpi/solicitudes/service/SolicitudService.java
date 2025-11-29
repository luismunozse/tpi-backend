package ar.edu.utn.frc.backend.tpi.solicitudes.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Cliente;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoSolicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Ruta;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Solicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Tramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.ClienteRepository;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.ContenedorRepository;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.RutaRepository;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.SolicitudRepository;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar el ciclo de vida completo de las solicitudes de transporte.
 *
 * Casos de uso implementados:
 * - CU-01: Registrar solicitud de transporte
 * - CU-02: Consultar estado de transporte
 * - CU-04: Asignar ruta a solicitud
 * - CU-05: Consultar contenedores pendientes
 * - CU-07: Registrar inicio de tramo
 * - CU-08: Registrar fin de tramo
 * - CU-10: Registrar costo y tiempo reales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;

    /**
     * CU-01: Registrar solicitud de transporte.
     * Crea una nueva solicitud asociada a un cliente y contenedor existentes.
     * La solicitud se crea en estado PENDIENTE.
     *
     * @param solicitudRequest datos de la solicitud
     * @return SolicitudResponse con los datos de la solicitud creada
     * @throws IllegalArgumentException si el cliente o contenedor no existen
     */
    @Transactional
    public SolicitudResponse crearSolicitud(SolicitudRequest solicitudRequest) {
        log.info("Creando solicitud para cliente {} y contenedor {}",
                solicitudRequest.getClienteId(), solicitudRequest.getContenedorId());

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(solicitudRequest.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado con id: " + solicitudRequest.getClienteId()));

        // Validar que el contenedor existe
        Contenedor contenedor = contenedorRepository.findById(solicitudRequest.getContenedorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contenedor no encontrado con id: " + solicitudRequest.getContenedorId()));

        // Crear la solicitud
        Solicitud solicitud = Solicitud.builder()
                .cliente(cliente)
                .contenedor(contenedor)
                .origenDireccion(solicitudRequest.getOrigenDireccion())
                .origenLatitud(solicitudRequest.getOrigenLatitud())
                .origenLongitud(solicitudRequest.getOrigenLongitud())
                .destinoDireccion(solicitudRequest.getDestinoDireccion())
                .destinoLatitud(solicitudRequest.getDestinoLatitud())
                .destinoLongitud(solicitudRequest.getDestinoLongitud())
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con id: {}", solicitudGuardada.getId());

        return mapToResponse(solicitudGuardada);
    }

    /**
     * CU-02: Consultar estado de transporte.
     * Obtiene el detalle completo de una solicitud por su ID.
     *
     * @param id identificador de la solicitud
     * @return SolicitudResponse con los datos de la solicitud
     * @throws IllegalArgumentException si la solicitud no existe
     */
    @Transactional(readOnly = true)
    public SolicitudResponse obtenerSolicitudPorId(Long id) {
        log.info("Consultando solicitud con id: {}", id);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud no encontrada con id: " + id));

        return mapToResponse(solicitud);
    }

    /**
     * CU-02: Consultar todas las solicitudes de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de solicitudes del cliente
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerSolicitudesPorCliente(Long clienteId) {
        log.info("Consultando solicitudes del cliente: {}", clienteId);

        // Validar que el cliente existe
        if (!clienteRepository.existsById(clienteId)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + clienteId);
        }

        List<Solicitud> solicitudes = solicitudRepository.findAll().stream()
                .filter(s -> s.getCliente().getId().equals(clienteId))
                .collect(Collectors.toList());

        return solicitudes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * CU-05: Consultar contenedores pendientes.
     * Retorna todas las solicitudes que no están en estado FINALIZADA.
     *
     * @return lista de solicitudes pendientes
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerSolicitudesPendientes() {
        log.info("Consultando solicitudes pendientes");

        List<Solicitud> solicitudes = solicitudRepository.findAll().stream()
                .filter(s -> !EstadoSolicitud.FINALIZADA.equals(s.getEstado()))
                .collect(Collectors.toList());

        return solicitudes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * CU-05: Consultar solicitudes por estado.
     *
     * @param estado estado de la solicitud
     * @return lista de solicitudes con el estado especificado
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerSolicitudesPorEstado(EstadoSolicitud estado) {
        log.info("Consultando solicitudes con estado: {}", estado);

        List<Solicitud> solicitudes = solicitudRepository.findAll().stream()
                .filter(s -> estado.equals(s.getEstado()))
                .collect(Collectors.toList());

        return solicitudes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * CU-04: Asignar ruta a una solicitud.
     * La ruta debe tener al menos un tramo y estar asociada a la solicitud.
     * Al asignar la ruta, la solicitud pasa a estado PROGRAMADA.
     *
     * @param solicitudId identificador de la solicitud
     * @param rutaId identificador de la ruta
     * @return SolicitudResponse actualizada
     * @throws IllegalArgumentException si la solicitud o ruta no existen
     * @throws IllegalStateException si la solicitud no está en estado PENDIENTE
     */
    @Transactional
    public SolicitudResponse asignarRuta(Long solicitudId, Long rutaId) {
        log.info("Asignando ruta {} a solicitud {}", rutaId, solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud no encontrada con id: " + solicitudId));

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ruta no encontrada con id: " + rutaId));

        // Validar que la solicitud está en estado PENDIENTE
        if (!EstadoSolicitud.PENDIENTE.equals(solicitud.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede asignar ruta a solicitudes en estado PENDIENTE. Estado actual: "
                    + solicitud.getEstado());
        }

        // Validar que la ruta tiene tramos
        if (ruta.getTramos() == null || ruta.getTramos().isEmpty()) {
            throw new IllegalArgumentException(
                    "La ruta debe tener al menos un tramo");
        }

        // Asignar la ruta y actualizar datos estimados
        solicitud.setRuta(ruta);
        solicitud.setCostoEstimado(ruta.getCostoEstimado());
        solicitud.setTiempoEstimadoHoras(ruta.getTiempoEstimadoHoras());
        solicitud.setEstado(EstadoSolicitud.PROGRAMADA);

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);
        log.info("Ruta asignada exitosamente a solicitud {}", solicitudId);

        return mapToResponse(solicitudActualizada);
    }

    /**
     * CU-07: Registrar inicio de tramo.
     * Marca el inicio de un tramo y actualiza el estado de la solicitud a EN_TRANSITO
     * si es el primer tramo.
     *
     * @param tramoId identificador del tramo
     * @return SolicitudResponse actualizada
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no está en estado ASIGNADO
     */
    @Transactional
    public SolicitudResponse registrarInicioTramo(Long tramoId) {
        log.info("Registrando inicio de tramo {}", tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        // Validar que el tramo está en estado ASIGNADO
        if (!EstadoTramo.ASIGNADO.equals(tramo.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede iniciar tramos en estado ASIGNADO. Estado actual: "
                    + tramo.getEstado());
        }

        // Registrar inicio
        tramo.setFechaHoraInicio(LocalDateTime.now());
        tramo.setEstado(EstadoTramo.INICIADO);
        tramoRepository.save(tramo);

        // Actualizar estado de la solicitud a EN_TRANSITO
        Solicitud solicitud = tramo.getRuta().getSolicitud();
        if (EstadoSolicitud.PROGRAMADA.equals(solicitud.getEstado())) {
            solicitud.setEstado(EstadoSolicitud.EN_TRANSITO);
            solicitudRepository.save(solicitud);
        }

        log.info("Inicio de tramo {} registrado exitosamente", tramoId);
        return mapToResponse(solicitud);
    }

    /**
     * CU-08: Registrar fin de tramo.
     * Marca el fin de un tramo. Si es el último tramo de la ruta,
     * finaliza la solicitud y calcula el costo y tiempo reales.
     *
     * @param tramoId identificador del tramo
     * @return SolicitudResponse actualizada
     * @throws IllegalArgumentException si el tramo no existe
     * @throws IllegalStateException si el tramo no está en estado INICIADO
     */
    @Transactional
    public SolicitudResponse registrarFinTramo(Long tramoId) {
        log.info("Registrando fin de tramo {}", tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tramo no encontrado con id: " + tramoId));

        // Validar que el tramo está en estado INICIADO
        if (!EstadoTramo.INICIADO.equals(tramo.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede finalizar tramos en estado INICIADO. Estado actual: "
                    + tramo.getEstado());
        }

        // Registrar fin
        tramo.setFechaHoraFin(LocalDateTime.now());
        tramo.setEstado(EstadoTramo.FINALIZADO);
        tramoRepository.save(tramo);

        // Obtener la solicitud
        Solicitud solicitud = tramo.getRuta().getSolicitud();
        Ruta ruta = tramo.getRuta();

        // Verificar si todos los tramos están finalizados
        boolean todosFinalizados = ruta.getTramos().stream()
                .allMatch(t -> EstadoTramo.FINALIZADO.equals(t.getEstado()));

        if (todosFinalizados) {
            // CU-10: Registrar costo y tiempo reales
            finalizarSolicitud(solicitud, ruta);
        }

        log.info("Fin de tramo {} registrado exitosamente", tramoId);
        return mapToResponse(solicitud);
    }

    /**
     * CU-10: Registrar costo y tiempo reales al finalizar la solicitud.
     * Calcula el costo real sumando los costos de todos los tramos.
     * Calcula el tiempo real desde la fecha de creación hasta el fin del último tramo.
     *
     * @param solicitud solicitud a finalizar
     * @param ruta ruta de la solicitud
     */
    private void finalizarSolicitud(Solicitud solicitud, Ruta ruta) {
        log.info("Finalizando solicitud {}", solicitud.getId());

        // Calcular costo real (suma de costos reales de todos los tramos)
        Double costoReal = ruta.getTramos().stream()
                .map(Tramo::getCostoReal)
                .filter(costo -> costo != null)
                .reduce(0.0, Double::sum);

        // Calcular tiempo real (desde fecha de creación hasta fin del último tramo)
        LocalDateTime fechaCreacion = solicitud.getFechaCreacion();
        LocalDateTime fechaFinUltimoTramo = ruta.getTramos().stream()
                .map(Tramo::getFechaHoraFin)
                .filter(fecha -> fecha != null)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        long minutosTranscurridos = ChronoUnit.MINUTES.between(fechaCreacion, fechaFinUltimoTramo);
        Double tiempoRealHoras = minutosTranscurridos / 60.0;

        // Actualizar solicitud
        solicitud.setCostoFinal(costoReal);
        solicitud.setTiempoRealHoras(tiempoRealHoras);
        solicitud.setEstado(EstadoSolicitud.FINALIZADA);

        solicitudRepository.save(solicitud);
        log.info("Solicitud {} finalizada. Costo real: {}, Tiempo real: {} horas",
                solicitud.getId(), costoReal, tiempoRealHoras);
    }

    /**
     * Actualizar estado de una solicitud.
     *
     * @param solicitudId identificador de la solicitud
     * @param nuevoEstado nuevo estado
     * @return SolicitudResponse actualizada
     * @throws IllegalArgumentException si la solicitud no existe
     */
    @Transactional
    public SolicitudResponse actualizarEstado(Long solicitudId, EstadoSolicitud nuevoEstado) {
        log.info("Actualizando estado de solicitud {} a {}", solicitudId, nuevoEstado);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud no encontrada con id: " + solicitudId));

        solicitud.setEstado(nuevoEstado);
        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        return mapToResponse(solicitudActualizada);
    }

    /**
     * Actualizar costos y tiempos estimados de una solicitud.
     * Útil cuando se recalculan los valores desde el microservicio de costos.
     *
     * @param solicitudId identificador de la solicitud
     * @param costoEstimado costo estimado
     * @param tiempoEstimadoHoras tiempo estimado en horas
     * @return SolicitudResponse actualizada
     */
    @Transactional
    public SolicitudResponse actualizarEstimaciones(Long solicitudId,
            Double costoEstimado, Double tiempoEstimadoHoras) {
        log.info("Actualizando estimaciones de solicitud {}", solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud no encontrada con id: " + solicitudId));

        solicitud.setCostoEstimado(costoEstimado);
        solicitud.setTiempoEstimadoHoras(tiempoEstimadoHoras);

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);
        return mapToResponse(solicitudActualizada);
    }

    /**
     * Obtener todas las solicitudes del sistema.
     *
     * @return lista de todas las solicitudes
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerTodasLasSolicitudes() {
        log.info("Consultando todas las solicitudes");

        return solicitudRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapea una entidad Solicitud a un DTO SolicitudResponse.
     *
     * @param solicitud entidad a mapear
     * @return SolicitudResponse
     */
    private SolicitudResponse mapToResponse(Solicitud solicitud) {
        return ar.edu.utn.frc.backend.tpi.solicitudes.mapper.SolicitudMapper.toResponse(solicitud);
    }
}
