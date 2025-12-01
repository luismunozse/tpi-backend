package ar.edu.utn.frc.backend.tpi.flota.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.tpi.flota.dto.AsignacionCamionRequest;
import ar.edu.utn.frc.backend.tpi.flota.dto.CamionDto;
import ar.edu.utn.frc.backend.tpi.flota.model.EstadoCamion;
import ar.edu.utn.frc.backend.tpi.flota.service.CamionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Tag(name = "Camiones", description = "Gestion de camiones y disponibilidad")
public class CamionController {

    private final CamionService camionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear camion")
    public ResponseEntity<CamionDto> crear(@Valid @RequestBody CamionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(camionService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Listar camiones")
    public ResponseEntity<List<CamionDto>> listar(
            @RequestParam(value = "estado", required = false) EstadoCamion estado) {
        List<CamionDto> result = (estado == null)
                ? camionService.listarTodos()
                : camionService.listarPorEstado(estado);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Listar camiones disponibles", description = "Filtra camiones disponibles por capacidad opcional")
    public ResponseEntity<List<CamionDto>> listarDisponibles(
            @RequestParam(value = "pesoKg", required = false) Double pesoKg,
            @RequestParam(value = "volumenM3", required = false) Double volumenM3) {
        return ResponseEntity.ok(camionService.disponiblesPorCapacidad(pesoKg, volumenM3));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener camion por id")
    public ResponseEntity<CamionDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar camion")
    public ResponseEntity<CamionDto> actualizar(@PathVariable Long id, @Valid @RequestBody CamionDto dto) {
        return ResponseEntity.ok(camionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar camion")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        camionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Asignar camión", description = "Marca camión como ocupado validando capacidad")
    public ResponseEntity<CamionDto> asignar(@PathVariable Long id, @Valid @RequestBody AsignacionCamionRequest request) {
        return ResponseEntity.ok(camionService.asignar(id, request));
    }

    @PostMapping("/{id}/liberar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Liberar camión", description = "Marca camión como disponible")
    public ResponseEntity<CamionDto> liberar(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.liberar(id));
    }
}
