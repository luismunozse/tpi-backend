package ar.edu.utn.frc.backend.tpi.flota.controller;

import ar.edu.utn.frc.backend.tpi.flota.dto.TransportistaDto;
import ar.edu.utn.frc.backend.tpi.flota.service.TransportistaService;
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

/**
 * Controlador REST para gestión de transportistas.
 */
@RestController
@RequestMapping("/api/transportistas")
@RequiredArgsConstructor
@Tag(name = "Transportistas", description = "Gestión de transportistas para asignación a camiones")
public class TransportistaController {

    private final TransportistaService transportistaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Listar todos los transportistas",
            description = "Obtiene todos los transportistas del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de transportistas")
    public ResponseEntity<List<TransportistaDto>> listar() {
        return ResponseEntity.ok(transportistaService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener transportista por ID",
            description = "Consulta un transportista específico por su identificador")
    @ApiResponse(responseCode = "200", description = "Transportista encontrado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    public ResponseEntity<TransportistaDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transportistaService.obtenerPorId(id));
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Buscar transportista por DNI",
            description = "Consulta un transportista por su número de DNI")
    @ApiResponse(responseCode = "200", description = "Transportista encontrado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    public ResponseEntity<TransportistaDto> obtenerPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(transportistaService.obtenerPorDni(dni));
    }

    @GetMapping("/estado/{activo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar transportistas por estado",
            description = "Obtiene todos los transportistas activos o inactivos")
    @ApiResponse(responseCode = "200", description = "Lista de transportistas")
    public ResponseEntity<List<TransportistaDto>> listarPorEstado(
            @PathVariable @Parameter(description = "true para activos, false para inactivos") Boolean activo) {
        return ResponseEntity.ok(transportistaService.listarPorEstado(activo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear transportista",
            description = "Registra un nuevo transportista en el sistema (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Transportista creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o DNI/email/licencia duplicados")
    public ResponseEntity<TransportistaDto> crear(@Valid @RequestBody TransportistaDto dto) {
        TransportistaDto transportista = transportistaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transportista);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar transportista",
            description = "Actualiza los datos de un transportista existente (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Transportista actualizado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o DNI/email/licencia duplicados")
    public ResponseEntity<TransportistaDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransportistaDto dto) {
        return ResponseEntity.ok(transportistaService.actualizar(id, dto));
    }

    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar transportista",
            description = "Marca un transportista como inactivo (soft delete) - (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Transportista desactivado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    public ResponseEntity<TransportistaDto> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(transportistaService.desactivar(id));
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar transportista",
            description = "Marca un transportista como activo (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Transportista activado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    public ResponseEntity<TransportistaDto> activar(@PathVariable Long id) {
        return ResponseEntity.ok(transportistaService.activar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar transportista",
            description = "Elimina físicamente un transportista del sistema. Solo se pueden eliminar transportistas sin camiones asignados (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Transportista eliminado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    @ApiResponse(responseCode = "400", description = "Transportista tiene camiones asignados")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transportistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
