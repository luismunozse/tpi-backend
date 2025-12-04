package ar.edu.utn.frc.backend.tpi.solicitudes.controller;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.TramoRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.TramoResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.service.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tramos")
@RequiredArgsConstructor
@Tag(name = "Tramos", description = "Gestión de tramos de rutas y asignación de camiones")
public class TramoController {

    private final TramoService tramoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los tramos",
            description = "Obtiene todos los tramos del sistema (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista de tramos")
    public ResponseEntity<List<TramoResponse>> obtenerTodosLosTramos() {
        return ResponseEntity.ok(tramoService.obtenerTodosLosTramos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener tramo por ID",
            description = "Consulta un tramo específico por su identificador")
    @ApiResponse(responseCode = "200", description = "Tramo encontrado")
    @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    public ResponseEntity<TramoResponse> obtenerTramoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tramoService.obtenerTramoPorId(id));
    }

    @GetMapping("/ruta/{rutaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener tramos de una ruta",
            description = "Lista todos los tramos de una ruta específica")
    @ApiResponse(responseCode = "200", description = "Lista de tramos de la ruta")
    public ResponseEntity<List<TramoResponse>> obtenerTramosPorRuta(@PathVariable Long rutaId) {
        return ResponseEntity.ok(tramoService.obtenerTramosPorRuta(rutaId));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener tramos por estado",
            description = "Lista tramos filtrados por estado. Estados válidos: ESTIMADO, ASIGNADO, INICIADO, FINALIZADO")
    @ApiResponse(responseCode = "200", description = "Lista de tramos con el estado especificado")
    public ResponseEntity<List<TramoResponse>> obtenerTramosPorEstado(@PathVariable String estado) {
        EstadoTramo estadoTramo = EstadoTramo.valueOf(estado.toUpperCase());
        return ResponseEntity.ok(tramoService.obtenerTramosPorEstado(estadoTramo));
    }

    @GetMapping("/camion/{camionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener tramos asignados a un camión",
            description = "Lista todos los tramos asignados a un camión específico. Útil para transportistas ver sus asignaciones.")
    @ApiResponse(responseCode = "200", description = "Lista de tramos del camión")
    public ResponseEntity<List<TramoResponse>> obtenerTramosPorCamion(@PathVariable Long camionId) {
        return ResponseEntity.ok(tramoService.obtenerTramosPorCamion(camionId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear tramo",
            description = "Crea un nuevo tramo asociado a una ruta (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Tramo creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<TramoResponse> crearTramo(@Valid @RequestBody TramoRequest request) {
        TramoResponse tramo = tramoService.crearTramo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tramo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar tramo",
            description = "Actualiza los datos de un tramo. No se puede actualizar si ya fue iniciado o finalizado (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Tramo actualizado")
    @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    @ApiResponse(responseCode = "400", description = "Tramo no puede ser actualizado en su estado actual")
    public ResponseEntity<TramoResponse> actualizarTramo(
            @PathVariable Long id,
            @Valid @RequestBody TramoRequest request) {
        return ResponseEntity.ok(tramoService.actualizarTramo(id, request));
    }

    @PutMapping("/{tramoId}/asignar-camion/{camionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[CU-06] Asignar camión a un tramo",
            description = "Asigna un camión específico a un tramo de traslado. El tramo debe estar en estado ESTIMADO. (Operador/Administrador)")
    @ApiResponse(responseCode = "200", description = "Camión asignado exitosamente")
    @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    @ApiResponse(responseCode = "400", description = "El tramo no está en estado ESTIMADO o el camión no es válido")
    public ResponseEntity<TramoResponse> asignarCamion(
            @PathVariable @Parameter(description = "ID del tramo") Long tramoId,
            @PathVariable @Parameter(description = "ID del camión") Long camionId) {
        return ResponseEntity.ok(tramoService.asignarCamion(tramoId, camionId));
    }

    @PostMapping("/{tramoId}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "[CU-07] Iniciar tramo",
            description = "Registra el inicio de un tramo de transporte. El tramo debe estar en estado ASIGNADO y tener un camión asignado.")
    @ApiResponse(responseCode = "200", description = "Tramo iniciado exitosamente")
    @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    @ApiResponse(responseCode = "400", description = "El tramo no puede ser iniciado en su estado actual")
    public ResponseEntity<TramoResponse> iniciarTramo(@PathVariable Long tramoId) {
        return ResponseEntity.ok(tramoService.iniciarTramo(tramoId));
    }

    @PostMapping("/{tramoId}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "[CU-08] Finalizar tramo",
            description = "Registra el fin de un tramo de transporte. El tramo debe estar en estado INICIADO.")
    @ApiResponse(responseCode = "200", description = "Tramo finalizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    @ApiResponse(responseCode = "400", description = "El tramo no puede ser finalizado en su estado actual")
    public ResponseEntity<TramoResponse> finalizarTramo(@PathVariable Long tramoId) {
        return ResponseEntity.ok(tramoService.finalizarTramo(tramoId));
    }

    @PutMapping("/{tramoId}/costo-real")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar costo real del tramo",
            description = "Registra el costo real de un tramo después de finalizado (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Costo real actualizado")
    @ApiResponse(responseCode = "400", description = "Solo se puede actualizar costo real de tramos finalizados")
    public ResponseEntity<TramoResponse> actualizarCostoReal(
            @PathVariable Long tramoId,
            @RequestParam Double costoReal) {
        return ResponseEntity.ok(tramoService.actualizarCostoReal(tramoId, costoReal));
    }

    @PutMapping("/{tramoId}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado de tramo",
            description = "Cambia manualmente el estado de un tramo (solo para casos especiales - ADMIN)")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    public ResponseEntity<TramoResponse> cambiarEstado(
            @PathVariable Long tramoId,
            @RequestParam String nuevoEstado) {
        EstadoTramo estado = EstadoTramo.valueOf(nuevoEstado.toUpperCase());
        return ResponseEntity.ok(tramoService.cambiarEstado(tramoId, estado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar tramo",
            description = "Elimina un tramo del sistema. Solo se pueden eliminar tramos en estado ESTIMADO (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Tramo eliminado")
    @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    @ApiResponse(responseCode = "400", description = "Tramo no puede ser eliminado en su estado actual")
    public ResponseEntity<Void> eliminarTramo(@PathVariable Long id) {
        tramoService.eliminarTramo(id);
        return ResponseEntity.noContent().build();
    }
}
