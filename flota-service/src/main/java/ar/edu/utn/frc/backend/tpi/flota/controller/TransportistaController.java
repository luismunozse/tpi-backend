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
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.tpi.flota.dto.TransportistaDto;
import ar.edu.utn.frc.backend.tpi.flota.service.TransportistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transportistas")
@RequiredArgsConstructor
@Tag(name = "Transportistas", description = "Gestion de transportistas")
public class TransportistaController {

    private final TransportistaService transportistaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear transportista")
    public ResponseEntity<TransportistaDto> crear(@Valid @RequestBody TransportistaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportistaService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Listar transportistas")
    public ResponseEntity<List<TransportistaDto>> listar() {
        return ResponseEntity.ok(transportistaService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener transportista por id")
    public ResponseEntity<TransportistaDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(transportistaService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar transportista")
    public ResponseEntity<TransportistaDto> actualizar(@PathVariable Long id, @Valid @RequestBody TransportistaDto dto) {
        return ResponseEntity.ok(transportistaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar transportista")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transportistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
