package ar.edu.utn.frc.backend.tpi.transportes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.transportes.dto.ClienteRequest;
import ar.edu.utn.frc.backend.tpi.transportes.dto.ClienteResponse;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Cliente;
import ar.edu.utn.frc.backend.tpi.transportes.mapper.ClienteMapper;
import ar.edu.utn.frc.backend.tpi.transportes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de clientes.
 * Implementa operaciones CRUD y lógica de negocio relacionada.
 *
 * Casos de uso relacionados:
 * - CU-01: Registro de clientes al crear solicitudes
 * - Gestión de datos de clientes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * Crear un nuevo cliente.
     * Valida que el email no esté en uso.
     *
     * @param request datos del cliente
     * @return ClienteResponse con los datos del cliente creado
     * @throws IllegalArgumentException si el email ya existe
     */
    @Transactional
    public ClienteResponse crearCliente(ClienteRequest request) {
        log.info("Creando cliente con email: {}", request.getEmail());

        // Validar que el email no exista
        if (clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el email: " + request.getEmail());
        }

        Cliente cliente = ClienteMapper.toEntity(request);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        log.info("Cliente creado con id: {}", clienteGuardado.getId());
        return ClienteMapper.toResponse(clienteGuardado);
    }

    /**
     * Obtener un cliente por su ID.
     *
     * @param id identificador del cliente
     * @return ClienteResponse con los datos del cliente
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Transactional(readOnly = true)
    public ClienteResponse obtenerClientePorId(Long id) {
        log.info("Consultando cliente con id: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado con id: " + id));

        return ClienteMapper.toResponse(cliente);
    }

    /**
     * Obtener un cliente por su email.
     *
     * @param email email del cliente
     * @return ClienteResponse con los datos del cliente
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Transactional(readOnly = true)
    public ClienteResponse obtenerClientePorEmail(String email) {
        log.info("Consultando cliente con email: {}", email);

        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado con email: " + email));

        return ClienteMapper.toResponse(cliente);
    }

    /**
     * Obtener todos los clientes del sistema.
     *
     * @return lista de todos los clientes
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodosLosClientes() {
        log.info("Consultando todos los clientes");

        return clienteRepository.findAll().stream()
                .map(ClienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar datos de un cliente existente.
     * No permite cambiar el email si ya está en uso por otro cliente.
     *
     * @param id identificador del cliente a actualizar
     * @param request nuevos datos del cliente
     * @return ClienteResponse con los datos actualizados
     * @throws IllegalArgumentException si el cliente no existe o el email está en uso
     */
    @Transactional
    public ClienteResponse actualizarCliente(Long id, ClienteRequest request) {
        log.info("Actualizando cliente con id: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado con id: " + id));

        // Validar que el nuevo email no esté en uso por otro cliente
        if (!cliente.getEmail().equals(request.getEmail())) {
            clienteRepository.findByEmail(request.getEmail()).ifPresent(c -> {
                throw new IllegalArgumentException(
                        "El email " + request.getEmail() + " ya está en uso por otro cliente");
            });
        }

        ClienteMapper.updateEntity(cliente, request);
        Cliente clienteActualizado = clienteRepository.save(cliente);

        log.info("Cliente actualizado con id: {}", clienteActualizado.getId());
        return ClienteMapper.toResponse(clienteActualizado);
    }

    /**
     * Eliminar un cliente del sistema.
     * IMPORTANTE: Solo puede eliminarse si no tiene solicitudes asociadas.
     *
     * @param id identificador del cliente a eliminar
     * @throws IllegalArgumentException si el cliente no existe
     * @throws IllegalStateException si el cliente tiene solicitudes asociadas
     */
    @Transactional
    public void eliminarCliente(Long id) {
        log.info("Eliminando cliente con id: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado con id: " + id));

        // TODO: Validar que no tenga solicitudes asociadas
        // Esto se puede hacer consultando SolicitudRepository cuando esté disponible
        // if (solicitudRepository.existsByClienteId(id)) {
        //     throw new IllegalStateException("No se puede eliminar el cliente porque tiene solicitudes asociadas");
        // }

        clienteRepository.delete(cliente);
        log.info("Cliente eliminado con id: {}", id);
    }

    /**
     * Verificar si existe un cliente con el email dado.
     *
     * @param email email a verificar
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existeClientePorEmail(String email) {
        return clienteRepository.findByEmail(email).isPresent();
    }

    /**
     * Obtener o crear un cliente por email.
     * Si el cliente existe, lo retorna; si no, lo crea con los datos provistos.
     * Útil para el flujo de creación de solicitudes (CU-01).
     *
     * @param request datos del cliente
     * @return ClienteResponse del cliente existente o recién creado
     */
    @Transactional
    public ClienteResponse obtenerOCrearCliente(ClienteRequest request) {
        log.info("Obteniendo o creando cliente con email: {}", request.getEmail());

        return clienteRepository.findByEmail(request.getEmail())
                .map(cliente -> {
                    log.info("Cliente existente encontrado con id: {}", cliente.getId());
                    return ClienteMapper.toResponse(cliente);
                })
                .orElseGet(() -> {
                    log.info("Creando nuevo cliente");
                    return crearCliente(request);
                });
    }
}
