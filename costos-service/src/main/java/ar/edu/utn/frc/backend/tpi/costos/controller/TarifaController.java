package ar.edu.utn.frc.backend.tpi.costos.controller;

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

import ar.edu.utn.frc.backend.tpi.costos.dto.EstimacionCostoRequest;
import ar.edu.utn.frc.backend.tpi.costos.dto.EstimacionCostoResponse;
import ar.edu.utn.frc.backend.tpi.costos.dto.TarifaDto;
import ar.edu.utn.frc.backend.tpi.costos.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
@Tag(name = "Tarifas y costos", description = "Gestion de tarifas y estimaciones de costo/tiempo")
public class TarifaController {

    private final TarifaService tarifaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear tarifa", description = "Crea una nueva tarifa")
    public ResponseEntity<TarifaDto> crear(@Valid @RequestBody TarifaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifaService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar tarifas", description = "Obtiene la lista de todas las tarifas")
    public ResponseEntity<List<TarifaDto>> listar() {
        return ResponseEntity.ok(tarifaService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener tarifa por id", description = "Obtiene una tarifa especifica por su id")
    public ResponseEntity<TarifaDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(tarifaService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar tarifa", description = "Actualiza una tarifa existente por su id")
    public ResponseEntity<TarifaDto> actualizar(@PathVariable Long id, @Valid @RequestBody TarifaDto dto) {
        return ResponseEntity.ok(tarifaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar tarifa", description = "Elimina una tarifa por su id")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tarifaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/estimacion")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Estimar costo y tiempo", description = "Calcula costo total y tiempo estimado con parametros de camion y distancias")
    public ResponseEntity<EstimacionCostoResponse> estimar(@Valid @RequestBody EstimacionCostoRequest request) {
        return ResponseEntity.ok(tarifaService.estimar(request));
    }

    @PostMapping("/estimacion/geoloc")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @Operation(summary = "Estimar con geolocalizacion", description = "Calcula costo y tiempo usando distancia desde coordenadas (Google Maps)")
    public ResponseEntity<EstimacionCostoResponse> estimarConGeoloc(
            @Valid @RequestBody ar.edu.utn.frc.backend.tpi.costos.dto.EstimacionCostoGeoRequest request) {
        return ResponseEntity.ok(tarifaService.estimarConGeolocalizacion(request));
    }
}
