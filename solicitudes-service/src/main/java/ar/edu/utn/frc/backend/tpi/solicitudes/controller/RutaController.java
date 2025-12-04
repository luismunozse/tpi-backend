package ar.edu.utn.frc.backend.tpi.solicitudes.controller;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.RutaRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.RutaResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Tag(name = "Rutas", description = "Gestión de rutas tentativas con tramos, tiempos y costos estimados")
public class RutaController {

    private final RutaService rutaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las rutas",
            description = "Devuelve todas las rutas con sus tramos, tiempos y costos estimados.")
    @ApiResponse(responseCode = "200", description = "Listado de rutas")
    public ResponseEntity<List<RutaResponse>> obtenerRutas() {
        return ResponseEntity.ok(rutaService.obtenerTodasLasRutas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener ruta por ID",
            description = "Consulta una ruta específica con todos sus tramos.")
    @ApiResponse(responseCode = "200", description = "Ruta encontrada")
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    public ResponseEntity<RutaResponse> obtenerRutaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.obtenerRutaPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[CU-03] Crear ruta tentativa",
            description = "Crea una nueva ruta con sus tramos. Los tramos deben incluir tipo (ORIGEN_DESTINO, ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO), origen, destino y costo estimado.")
    @ApiResponse(responseCode = "201", description = "Ruta creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de ruta inválidos")
    public ResponseEntity<RutaResponse> crearRuta(@Valid @RequestBody RutaRequest request) {
        RutaResponse rutaCreada = rutaService.crearRuta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rutaCreada);
    }

    @PostMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[CU-04] Crear y asignar ruta a solicitud",
            description = "Crea una nueva ruta con sus tramos y la asigna automáticamente a una solicitud.")
    @ApiResponse(responseCode = "201", description = "Ruta creada y asignada exitosamente")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @ApiResponse(responseCode = "400", description = "Datos de ruta inválidos")
    public ResponseEntity<RutaResponse> crearRutaParaSolicitud(
            @PathVariable Long solicitudId,
            @Valid @RequestBody RutaRequest request) {
        RutaResponse rutaCreada = rutaService.crearRutaParaSolicitud(solicitudId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rutaCreada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar ruta",
            description = "Actualiza los datos generales de una ruta (distancia, tiempo, costo). No se pueden actualizar rutas con tramos en proceso.")
    @ApiResponse(responseCode = "200", description = "Ruta actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    @ApiResponse(responseCode = "400", description = "Ruta con tramos en proceso, no se puede actualizar")
    public ResponseEntity<RutaResponse> actualizarRuta(
            @PathVariable Long id,
            @Valid @RequestBody RutaRequest request) {
        return ResponseEntity.ok(rutaService.actualizarRuta(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar ruta",
            description = "Elimina una ruta del sistema. Solo se pueden eliminar rutas que no estén asignadas a ninguna solicitud.")
    @ApiResponse(responseCode = "204", description = "Ruta eliminada exitosamente")
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    @ApiResponse(responseCode = "400", description = "Ruta asignada a solicitud, no se puede eliminar")
    public ResponseEntity<Void> eliminarRuta(@PathVariable Long id) {
        rutaService.eliminarRuta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/progreso")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRANSPORTISTA')")
    @Operation(summary = "Obtener progreso de ruta",
            description = "Consulta el progreso de ejecución de una ruta (tramos completados, en proceso, etc.).")
    @ApiResponse(responseCode = "200", description = "Progreso de ruta")
    public ResponseEntity<RutaService.ProgresoRuta> obtenerProgresoRuta(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.obtenerProgresoRuta(id));
    }

    @PostMapping("/{id}/recalcular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recalcular métricas de ruta",
            description = "Recalcula el costo estimado total basándose en los costos de los tramos.")
    @ApiResponse(responseCode = "200", description = "Métricas recalculadas")
    public ResponseEntity<RutaResponse> recalcularMetricas(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.recalcularMetricas(id));
    }
}
