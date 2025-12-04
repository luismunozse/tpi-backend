package ar.edu.utn.frc.backend.tpi.solicitudes.controller;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ContenedorRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ContenedorResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoContenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.service.ContenedorService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contenedores")
@RequiredArgsConstructor
@Tag(name = "Contenedores", description = "Gestión de contenedores y consulta de estados y ubicaciones")
public class ContenedorController {

    private final ContenedorService contenedorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los contenedores",
            description = "Obtiene todos los contenedores del sistema (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista de contenedores")
    public ResponseEntity<List<ContenedorResponse>> obtenerTodosLosContenedores() {
        return ResponseEntity.ok(contenedorService.obtenerTodosLosContenedores());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA', 'CLIENTE')")
    @Operation(summary = "Obtener contenedor por ID",
            description = "Consulta un contenedor específico por su identificador")
    @ApiResponse(responseCode = "200", description = "Contenedor encontrado")
    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    public ResponseEntity<ContenedorResponse> obtenerContenedorPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contenedorService.obtenerContenedorPorId(id));
    }

    @GetMapping("/serie/{numeroSerie}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA', 'CLIENTE')")
    @Operation(summary = "Buscar contenedor por número de serie",
            description = "Consulta un contenedor por su número de serie único")
    @ApiResponse(responseCode = "200", description = "Contenedor encontrado")
    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    public ResponseEntity<ContenedorResponse> obtenerContenedorPorNumeroSerie(
            @PathVariable String numeroSerie) {
        return ResponseEntity.ok(contenedorService.obtenerContenedorPorNumeroSerie(numeroSerie));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Contenedores por cliente",
            description = "Obtiene todos los contenedores de un cliente específico")
    @ApiResponse(responseCode = "200", description = "Lista de contenedores del cliente")
    public ResponseEntity<List<ContenedorResponse>> obtenerContenedoresPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(contenedorService.obtenerContenedoresPorCliente(clienteId));
    }

    @GetMapping("/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Contenedores por estado",
            description = "Obtiene contenedores filtrados por estado. Estados válidos: REGISTRADO, LISTO_PARA_RETIRO, EN_TRASLADO, EN_DEPOSITO, ENTREGADO")
    @ApiResponse(responseCode = "200", description = "Lista de contenedores con el estado especificado")
    public ResponseEntity<List<ContenedorResponse>> obtenerContenedoresPorEstado(
            @RequestParam @Parameter(description = "Estado del contenedor") String estado) {
        EstadoContenedor estadoContenedor = EstadoContenedor.valueOf(estado.toUpperCase());
        return ResponseEntity.ok(contenedorService.obtenerContenedoresPorEstado(estadoContenedor));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTISTA')")
    @Operation(summary = "Consultar contenedores pendientes de entrega",
            description = "Obtiene todos los contenedores que NO están en estado ENTREGADO, con información de estado y ubicación actual. (Operador/Administrador)")
    @ApiResponse(responseCode = "200", description = "Lista de contenedores pendientes")
    public ResponseEntity<List<ContenedorResponse>> obtenerContenedoresPendientes() {
        // Obtener todos los contenedores que no están entregados
        List<ContenedorResponse> pendientes = contenedorService.obtenerTodosLosContenedores()
                .stream()
                .filter(c -> !c.getEstado().equals("ENTREGADO"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendientes);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear contenedor",
            description = "Registra un nuevo contenedor en el sistema (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Contenedor creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o número de serie duplicado")
    public ResponseEntity<ContenedorResponse> crearContenedor(
            @Valid @RequestBody ContenedorRequest request) {
        ContenedorResponse contenedor = contenedorService.crearContenedor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(contenedor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar contenedor",
            description = "Actualiza los datos de un contenedor existente (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Contenedor actualizado")
    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    @ApiResponse(responseCode = "400", description = "No se puede actualizar contenedor en tránsito o entregado")
    public ResponseEntity<ContenedorResponse> actualizarContenedor(
            @PathVariable Long id,
            @Valid @RequestBody ContenedorRequest request) {
        return ResponseEntity.ok(contenedorService.actualizarContenedor(id, request));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado de contenedor",
            description = "Actualiza el estado de un contenedor siguiendo las transiciones válidas (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Transición de estado inválida")
    public ResponseEntity<ContenedorResponse> cambiarEstadoContenedor(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        EstadoContenedor estado = EstadoContenedor.valueOf(nuevoEstado.toUpperCase());
        return ResponseEntity.ok(contenedorService.cambiarEstadoContenedor(id, estado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar contenedor",
            description = "Elimina un contenedor del sistema. Solo se pueden eliminar contenedores en estado REGISTRADO (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Contenedor eliminado")
    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    @ApiResponse(responseCode = "400", description = "Contenedor no puede ser eliminado")
    public ResponseEntity<Void> eliminarContenedor(@PathVariable Long id) {
        contenedorService.eliminarContenedor(id);
        return ResponseEntity.noContent().build();
    }
}
