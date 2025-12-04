package ar.edu.utn.frc.backend.tpi.solicitudes.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ClienteResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ContenedorRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ContenedorResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Cliente;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoSolicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Ruta;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Solicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Tramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.mapper.SolicitudMapper;
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
    private final ClienteService clienteService;
    private final ContenedorService contenedorService;

    /**
     * CU-01: Registrar solicitud de transporte.
     * Implementa creación atómica de cliente y contenedor (RF 1.1, RF 1.2).
     * Valida que el email del JWT coincida con el email del cliente en el request.
     * La solicitud se crea en estado BORRADOR.
     *
     * @param solicitudRequest datos de la solicitud (incluye cliente y contenedor completos)
     * @return SolicitudResponse con los datos de la solicitud creada
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws org.springframework.security.access.AccessDeniedException si el email no coincide
     */
    @Transactional
    public SolicitudResponse crearSolicitud(SolicitudRequest solicitudRequest) {
        log.info("Creando solicitud con patrón obtener-o-crear para cliente {} y contenedor {}",
                solicitudRequest.getCliente().getEmail(),
                solicitudRequest.getContenedor().getNumeroSerie());

        // Obtener JWT y extraer email del usuario autenticado
        Jwt jwt = getJwt();
        String emailToken = obtenerEmail(jwt);

        // Validar que el email del token coincida con el email del cliente en el request.
        // Si no coincide, consideramos que el cliente no está registrado en Keycloak y frenamos.
        if (emailToken == null || !emailToken.equalsIgnoreCase(solicitudRequest.getCliente().getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cliente no registrado en Keycloak. Vaya a Keycloak y regístrelo.");
        }

        // Obtener o crear cliente usando el patrón find-or-create
        ClienteResponse clienteResponse = clienteService.obtenerOCrearCliente(solicitudRequest.getCliente());
        log.info("Cliente obtenido/creado con id: {}", clienteResponse.getId());

        // Obtener el cliente como entidad para asociarlo a la solicitud
        Cliente cliente = clienteRepository.findById(clienteResponse.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Error al obtener cliente recién creado con id: " + clienteResponse.getId()));

        // Configurar el clienteId en el contenedor request antes de crear/obtener
        ContenedorRequest contenedorRequest = solicitudRequest.getContenedor();
        contenedorRequest.setClienteId(clienteResponse.getId());

        // Obtener o crear contenedor usando el patrón find-or-create
        ContenedorResponse contenedorResponse = contenedorService.obtenerOCrearContenedor(contenedorRequest);
        log.info("Contenedor obtenido/creado con id: {}", contenedorResponse.getId());

        // Obtener el contenedor como entidad para asociarlo a la solicitud
        Contenedor contenedor = contenedorRepository.findById(contenedorResponse.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Error al obtener contenedor recién creado con id: " + contenedorResponse.getId()));

        // Evitar solicitudes duplicadas para un contenedor que aun no fue entregado
        List<Solicitud> solicitudesActivas = solicitudRepository.findByContenedorIdAndEstadoNot(
                contenedor.getId(), EstadoSolicitud.ENTREGADA);
        if (!solicitudesActivas.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una solicitud activa para el contenedor " + contenedor.getNumeroSerie());
        }

        // Crear la solicitud con las entidades obtenidas/creadas
        Solicitud solicitud = Solicitud.builder()
                .cliente(cliente)
                .contenedor(contenedor)
                .origenDireccion(solicitudRequest.getOrigenDireccion())
                .origenLatitud(solicitudRequest.getOrigenLatitud())
                .origenLongitud(solicitudRequest.getOrigenLongitud())
                .destinoDireccion(solicitudRequest.getDestinoDireccion())
                .destinoLatitud(solicitudRequest.getDestinoLatitud())
                .destinoLongitud(solicitudRequest.getDestinoLongitud())
                .estado(EstadoSolicitud.BORRADOR)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con id: {} para cliente {} (email: {})",
                solicitudGuardada.getId(), cliente.getId(), cliente.getEmail());

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

        Jwt jwt = getJwt();
        validarPropietario(solicitud.getCliente(), jwt);

        return mapToResponse(solicitud);
    }

    /**
     * CU-02: Consultar todas las solicitudes de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de solicitudes del cliente
     */
    @Transactional(readOnly = true) // Solo lectura de datos 
    public List<SolicitudResponse> obtenerSolicitudesPorCliente(Long clienteId) {
        log.info("Consultando solicitudes del cliente: {}", clienteId);

        // Validar que el cliente existe
        if (!clienteRepository.existsById(clienteId)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + clienteId);
        }

        // Si es rol CLIENTE, solo puede consultar su propio clienteId
        Jwt jwt = getJwt();
        validarClienteId(jwt, clienteId);

        List<Solicitud> solicitudes = solicitudRepository.findByClienteId(clienteId);

        return solicitudes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * CU-05: Consultar contenedores pendientes.
     * Retorna todas las solicitudes que no están en estado ENTREGADA.
     *
     * @return lista de solicitudes pendientes
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerSolicitudesPendientes() {
        log.info("Consultando solicitudes pendientes");

        List<Solicitud> solicitudes = solicitudRepository.findByEstadoNot(EstadoSolicitud.ENTREGADA);

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

        List<Solicitud> solicitudes = solicitudRepository.findByEstado(estado);

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
     * @throws IllegalStateException si la solicitud no está en estado BORRADOR
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

        // Validar que la solicitud está en estado BORRADOR
        if (!EstadoSolicitud.BORRADOR.equals(solicitud.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede asignar ruta a solicitudes en estado BORRADOR. Estado actual: "
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
        solicitud.setEstado(EstadoSolicitud.ENTREGADA);

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
        return SolicitudMapper.toResponse(solicitud);
    }

    /**
     * Valida que, si el rol es CLIENTE, el recurso pertenece al subject autenticado.
     * Se asume que el claim "email" o "preferred_username" se corresponde con el email del cliente.
     */
    private void validarPropietario(Cliente cliente, Jwt jwt) {
        if (cliente == null || jwt == null) {
            return;
        }
        if (tieneRol("ADMIN") || tieneRol("TRANSPORTISTA")) {
            return;
        }
        if (tieneRol("CLIENTE")) {
            String emailToken = obtenerEmail(jwt);
            if (emailToken == null || !emailToken.equalsIgnoreCase(cliente.getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "No puedes acceder a una solicitud de otro cliente");
            }
        }
    }

    private void validarClienteId(Jwt jwt, Long clienteId) {
        if (jwt == null || clienteId == null) {
            return;
        }
        if (tieneRol("ADMIN")) {
            return;
        }
        if (tieneRol("CLIENTE")) {
            String emailToken = obtenerEmail(jwt);
            if (emailToken == null) {
                throw new org.springframework.security.access.AccessDeniedException("Token sin email");
            }
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + clienteId));
            if (!emailToken.equalsIgnoreCase(cliente.getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "No puedes acceder a otro cliente");
            }
        }
    }

    private boolean tieneRol(String rol) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }

    private Jwt getJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        return null;
    }

    private String obtenerEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) {
            return email;
        }
        return jwt.getClaimAsString("preferred_username");
     }
}
