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
import ar.edu.utn.frc.backend.tpi.solicitudes.service.CostoSolicitudService;
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
 * Roles:
 * - CLIENTE: crea solicitudes y consulta solo las propias.
 * - ADMIN: gestión completa y consultas globales.
 * - TRANSPORTISTA: seguimiento de tramos asignados (inicio/fin).
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
    private final CostoSolicitudService costoSolicitudService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Crear solicitud de transporte",
               description = "Registra una nueva solicitud de transporte de contenedor (CU-01). " +
                             "Implementa creación atómica: si el cliente o contenedor no existen, se crean automáticamente (RF 1.1, RF 1.2).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente",
                     content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Email del cliente no coincide con el usuario autenticado")
    })
    public ResponseEntity<SolicitudResponse> crearSolicitud(
            @Valid @RequestBody SolicitudRequest request) {

        log.info("REST: Creando solicitud para cliente (email: {}) y contenedor (serie: {})",
                request.getCliente().getEmail(),
                request.getContenedor().getNumeroSerie());

        SolicitudResponse response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * CU-02: Obtener una solicitud por ID.
     * Roles: CLIENTE (solo sus solicitudes), ADMIN (todas), TRANSPORTISTA (seguimiento).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'TRANSPORTISTA')")
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
     * Roles: CLIENTE (solo su propio clienteId), ADMIN (cualquier cliente).
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
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

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar solicitudes pendientes",
               description = "Obtiene todas las solicitudes que no están entregadas (CU-05)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitudes pendientes obtenidas"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<SolicitudResponse>> obtenerSolicitudesPendientes() {
        log.info("REST: Consultando solicitudes pendientes");
        List<SolicitudResponse> response = solicitudService.obtenerSolicitudesPendientes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/por-estado")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/{solicitudId}/ruta/{rutaId}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/tramos/{tramoId}/inicio")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA', 'ADMIN')")
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

    @PostMapping("/tramos/{tramoId}/fin")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA', 'ADMIN')")
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

    @PutMapping("/{solicitudId}/estado")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/{solicitudId}/estimaciones")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/{solicitudId}/costo-total")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Calcular costo total de entrega",
               description = "Calcula el costo total de la entrega incluyendo: " +
                             "recorrido total (distancia entre origen → depósitos y depósitos → destino), " +
                             "peso y volumen del contenedor, " +
                             "y estadía en depósitos (calculada a partir de la diferencia entre fechas reales de entrada y salida). " +
                             "(Operador/Administrador/Cliente)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Costo total calculado",
                     content = @Content(schema = @Schema(implementation = CostoSolicitudService.DesgloseCostoTotal.class))),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada o sin ruta asignada"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<CostoSolicitudService.DesgloseCostoTotal> calcularCostoTotal(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId) {

        log.info("REST: Calculando costo total de solicitud {}", solicitudId);
        CostoSolicitudService.DesgloseCostoTotal desglose = costoSolicitudService.calcularCostoTotal(solicitudId);
        return ResponseEntity.ok(desglose);
    }
}
