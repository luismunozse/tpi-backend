package ar.edu.utn.frc.backend.tpi.solicitudes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.SolicitudResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoSolicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para gestión de solicitudes de transporte.
 * Expone endpoints para crear, consultar y gestionar solicitudes.
 *
 * Casos de uso implementados:
 * - CU-01: Registrar solicitud de transporte (Cliente)
 * - CU-02: Consultar estado de transporte (Cliente)
 * - CU-04: Asignar ruta a solicitud (Operador/Administrador)
 * - CU-05: Consultar contenedores pendientes (Operador/Administrador)
 * - CU-07: Registrar inicio de tramo (Transportista)
 * - CU-08: Registrar fin de tramo (Transportista)
 */
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Solicitudes", description = "API para gestión de solicitudes de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class SolicitudController {

    private final SolicitudService solicitudService;

    /**
     * CU-01: Crear una nueva solicitud de transporte.
     * Disponible para clientes.
     *
     * @param request datos de la solicitud
     * @return solicitud creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Crear solicitud de transporte",
               description = "Registra una nueva solicitud de transporte de contenedor (CU-01)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Cliente o contenedor no encontrado")
    })
    public ResponseEntity<SolicitudResponse> crearSolicitud(
            @Valid @RequestBody SolicitudRequest request) {

        log.info("REST: Creando solicitud para cliente {} y contenedor {}",
                request.getClienteId(), request.getContenedorId());

        SolicitudResponse response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * CU-02: Obtener una solicitud por ID.
     * Disponible para clientes (solo sus solicitudes), operadores y transportistas.
     *
     * @param id identificador de la solicitud
     * @return solicitud encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Consultar solicitud por ID",
               description = "Obtiene el detalle completo de una solicitud de transporte (CU-02)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud encontrada",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<SolicitudResponse> obtenerSolicitudPorId(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {

        log.info("REST: Consultando solicitud {}", id);
        SolicitudResponse response = solicitudService.obtenerSolicitudPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * CU-02: Obtener solicitudes de un cliente.
     * Disponible para clientes (solo sus propias solicitudes) y operadores.
     *
     * @param clienteId identificador del cliente
     * @return lista de solicitudes del cliente
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Consultar solicitudes de un cliente",
               description = "Obtiene todas las solicitudes de un cliente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitudes encontradas"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<List<SolicitudResponse>> obtenerSolicitudesPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {

        log.info("REST: Consultando solicitudes del cliente {}", clienteId);
        List<SolicitudResponse> response = solicitudService.obtenerSolicitudesPorCliente(clienteId);
        return ResponseEntity.ok(response);
    }

    /**
     * CU-05: Obtener solicitudes pendientes.
     * Disponible solo para operadores y administradores.
     *
     * @return lista de solicitudes pendientes
     */
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Consultar solicitudes pendientes",
               description = "Obtiene todas las solicitudes que no están finalizadas (CU-05)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitudes pendientes obtenidas"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<SolicitudResponse>> obtenerSolicitudesPendientes() {
        log.info("REST: Consultando solicitudes pendientes");
        List<SolicitudResponse> response = solicitudService.obtenerSolicitudesPendientes();
        return ResponseEntity.ok(response);
    }

    /**
     * CU-05: Obtener solicitudes por estado.
     * Disponible para operadores y administradores.
     *
     * @param estado estado de las solicitudes
     * @return lista de solicitudes con el estado especificado
     */
    @GetMapping("/por-estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Consultar solicitudes por estado",
               description = "Filtra solicitudes por su estado actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitudes encontradas"),
        @ApiResponse(responseCode = "400", description = "Estado inválido"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<SolicitudResponse>> obtenerSolicitudesPorEstado(
            @Parameter(description = "Estado de la solicitud")
            @RequestParam EstadoSolicitud estado) {

        log.info("REST: Consultando solicitudes con estado {}", estado);
        List<SolicitudResponse> response = solicitudService.obtenerSolicitudesPorEstado(estado);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todas las solicitudes del sistema.
     * Disponible solo para administradores y operadores.
     *
     * @return lista de todas las solicitudes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar todas las solicitudes",
               description = "Obtiene todas las solicitudes del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitudes obtenidas"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<SolicitudResponse>> obtenerTodasLasSolicitudes() {
        log.info("REST: Consultando todas las solicitudes");
        List<SolicitudResponse> response = solicitudService.obtenerTodasLasSolicitudes();
        return ResponseEntity.ok(response);
    }

    /**
     * CU-04: Asignar ruta a una solicitud.
     * Disponible solo para operadores y administradores.
     *
     * @param solicitudId identificador de la solicitud
     * @param rutaId identificador de la ruta
     * @return solicitud actualizada con la ruta asignada
     */
    @PutMapping("/{solicitudId}/ruta/{rutaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Asignar ruta a solicitud",
               description = "Asocia una ruta planificada a una solicitud (CU-04)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ruta asignada exitosamente",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "400", description = "Estado de solicitud no permite asignación"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Solicitud o ruta no encontrada")
    })
    public ResponseEntity<SolicitudResponse> asignarRuta(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId,
            @Parameter(description = "ID de la ruta") @PathVariable Long rutaId) {

        log.info("REST: Asignando ruta {} a solicitud {}", rutaId, solicitudId);
        SolicitudResponse response = solicitudService.asignarRuta(solicitudId, rutaId);
        return ResponseEntity.ok(response);
    }

    /**
     * CU-07: Registrar inicio de tramo.
     * Disponible para transportistas.
     *
     * @param tramoId identificador del tramo
     * @return solicitud actualizada
     */
    @PostMapping("/tramos/{tramoId}/inicio")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Registrar inicio de tramo",
               description = "Marca el inicio de un tramo de transporte (CU-07)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inicio de tramo registrado",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "400", description = "Estado del tramo no permite inicio"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    })
    public ResponseEntity<SolicitudResponse> registrarInicioTramo(
            @Parameter(description = "ID del tramo") @PathVariable Long tramoId) {

        log.info("REST: Registrando inicio de tramo {}", tramoId);
        SolicitudResponse response = solicitudService.registrarInicioTramo(tramoId);
        return ResponseEntity.ok(response);
    }

    /**
     * CU-08: Registrar fin de tramo.
     * Disponible para transportistas.
     *
     * @param tramoId identificador del tramo
     * @return solicitud actualizada
     */
    @PostMapping("/tramos/{tramoId}/fin")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Registrar fin de tramo",
               description = "Marca el fin de un tramo de transporte (CU-08)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fin de tramo registrado",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "400", description = "Estado del tramo no permite finalización"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    })
    public ResponseEntity<SolicitudResponse> registrarFinTramo(
            @Parameter(description = "ID del tramo") @PathVariable Long tramoId) {

        log.info("REST: Registrando fin de tramo {}", tramoId);
        SolicitudResponse response = solicitudService.registrarFinTramo(tramoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar estado de una solicitud.
     * Disponible solo para administradores y operadores.
     *
     * @param solicitudId identificador de la solicitud
     * @param nuevoEstado nuevo estado
     * @return solicitud actualizada
     */
    @PutMapping("/{solicitudId}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Actualizar estado de solicitud",
               description = "Cambia manualmente el estado de una solicitud")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "400", description = "Estado inválido"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<SolicitudResponse> actualizarEstado(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId,
            @Parameter(description = "Nuevo estado") @RequestParam EstadoSolicitud nuevoEstado) {

        log.info("REST: Actualizando estado de solicitud {} a {}", solicitudId, nuevoEstado);
        SolicitudResponse response = solicitudService.actualizarEstado(solicitudId, nuevoEstado);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar estimaciones de costo y tiempo.
     * Disponible para operadores y administradores.
     *
     * @param solicitudId identificador de la solicitud
     * @param costoEstimado costo estimado
     * @param tiempoEstimadoHoras tiempo estimado en horas
     * @return solicitud actualizada
     */
    @PutMapping("/{solicitudId}/estimaciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Actualizar estimaciones",
               description = "Actualiza costo y tiempo estimados de una solicitud")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estimaciones actualizadas",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<SolicitudResponse> actualizarEstimaciones(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId,
            @Parameter(description = "Costo estimado") @RequestParam Double costoEstimado,
            @Parameter(description = "Tiempo estimado en horas") @RequestParam Double tiempoEstimadoHoras) {

        log.info("REST: Actualizando estimaciones de solicitud {}", solicitudId);
        SolicitudResponse response = solicitudService.actualizarEstimaciones(
                solicitudId, costoEstimado, tiempoEstimadoHoras);
        return ResponseEntity.ok(response);
    }
}