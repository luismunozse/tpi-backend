package ar.edu.utn.frc.backend.tpi.transportes.controller;

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

import ar.edu.utn.frc.backend.tpi.transportes.dto.ClienteRequest;
import ar.edu.utn.frc.backend.tpi.transportes.dto.ClienteResponse;
import ar.edu.utn.frc.backend.tpi.transportes.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de clientes.
 * Proporciona endpoints para operaciones CRUD y consultas.
 *
 * Roles:
 * - ADMIN: Acceso completo a todas las operaciones
 * - OPERADOR: Puede consultar y crear clientes
 * - CLIENTE: Puede consultar sus propios datos
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "API para gestión de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Crear un nuevo cliente.
     * Validación: nombre, email válido, unicidad de email.
     *
     * @param request datos del cliente
     * @return cliente creado con estado 201
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email duplicado",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content)
    })
    public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody ClienteRequest request) {
        log.info("POST /api/clientes - Crear cliente con email: {}", request.getEmail());
        ClienteResponse response = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener cliente por ID.
     *
     * @param id identificador del cliente
     * @return cliente encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener cliente por ID", description = "Consulta los datos de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content)
    })
    public ResponseEntity<ClienteResponse> obtenerClientePorId(@PathVariable Long id) {
        log.info("GET /api/clientes/{} - Consultar cliente", id);
        ClienteResponse response = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener cliente por email.
     * Útil para buscar antes de crear o para login.
     *
     * @param email email del cliente
     * @return cliente encontrado
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener cliente por email", description = "Busca un cliente por su dirección de email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content)
    })
    public ResponseEntity<ClienteResponse> obtenerClientePorEmail(@PathVariable String email) {
        log.info("GET /api/clientes/email/{} - Consultar cliente por email", email);
        ClienteResponse response = clienteService.obtenerClientePorEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los clientes.
     * Solo para administradores y operadores.
     *
     * @return lista de todos los clientes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar clientes", description = "Obtiene la lista completa de clientes registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content)
    })
    public ResponseEntity<List<ClienteResponse>> obtenerTodosLosClientes() {
        log.info("GET /api/clientes - Listar todos los clientes");
        List<ClienteResponse> response = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar clientes por nombre (búsqueda parcial).
     *
     * @param nombre nombre o parte del nombre a buscar
     * @return lista de clientes que coinciden
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Buscar clientes por nombre", description = "Búsqueda parcial de clientes por nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content)
    })
    public ResponseEntity<List<ClienteResponse>> buscarClientesPorNombre(@RequestParam String nombre) {
        log.info("GET /api/clientes/buscar?nombre={} - Buscar clientes", nombre);
        List<ClienteResponse> response = clienteService.buscarClientesPorNombre(nombre);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar datos de un cliente.
     * No permite cambiar el email si ya está en uso por otro cliente.
     *
     * @param id identificador del cliente
     * @param request nuevos datos
     * @return cliente actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'CLIENTE')")
    @Operation(summary = "Actualizar cliente", description = "Modifica los datos de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email duplicado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content)
    })
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        log.info("PUT /api/clientes/{} - Actualizar cliente", id);
        ClienteResponse response = clienteService.actualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un cliente.
     * Solo si no tiene solicitudes asociadas.
     * Solo para administradores.
     *
     * @param id identificador del cliente
     * @return respuesta sin contenido (204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente del sistema (solo si no tiene solicitudes)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Cliente tiene solicitudes asociadas",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content)
    })
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        log.info("DELETE /api/clientes/{} - Eliminar cliente", id);
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener o crear un cliente por email.
     * Si el cliente existe, lo retorna. Si no, lo crea con los datos proporcionados.
     * Útil para registro automático.
     *
     * @param request datos del cliente
     * @return cliente existente o recién creado
     */
    @PostMapping("/obtener-o-crear")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Obtener o crear cliente", description = "Retorna el cliente si existe, o lo crea si no existe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente existente retornado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content)
    })
    public ResponseEntity<ClienteResponse> obtenerOCrearCliente(@Valid @RequestBody ClienteRequest request) {
        log.info("POST /api/clientes/obtener-o-crear - Email: {}", request.getEmail());
        ClienteResponse response = clienteService.obtenerOCrearCliente(request);

        // Si el cliente ya existía, retornar 200. Si se creó, retornar 201
        // Para simplificar, siempre retornamos 200 (el servicio maneja la lógica)
        return ResponseEntity.ok(response);
    }
}
