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

import ar.edu.utn.frc.backend.tpi.flota.dto.DepositoDto;
import ar.edu.utn.frc.backend.tpi.flota.service.DepositoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
@Tag(name = "Depósitos", description = "Gestión de depósitos y costos de estadía")
public class DepositoController {

    private final DepositoService depositoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear depósito")
    public ResponseEntity<DepositoDto> crear(@Valid @RequestBody DepositoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(depositoService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Listar depósitos")
    public ResponseEntity<List<DepositoDto>> listar() {
        return ResponseEntity.ok(depositoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener depósito por id")
    public ResponseEntity<DepositoDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(depositoService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar depósito")
    public ResponseEntity<DepositoDto> actualizar(@PathVariable Long id, @Valid @RequestBody DepositoDto dto) {
        return ResponseEntity.ok(depositoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar depósito")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        depositoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

