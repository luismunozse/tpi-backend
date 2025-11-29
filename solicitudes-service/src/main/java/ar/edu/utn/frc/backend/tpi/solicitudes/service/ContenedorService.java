package ar.edu.utn.frc.backend.tpi.solicitudes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ContenedorRequest;
import ar.edu.utn.frc.backend.tpi.solicitudes.dto.ContenedorResponse;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Cliente;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoContenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.mapper.ContenedorMapper;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.ClienteRepository;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de contenedores.
 * Implementa operaciones CRUD y lógica de validación de restricciones.
 *
 * Casos de uso relacionados:
 * - CU-01: Creación de contenedores al registrar solicitudes
 * - Gestión de estados de contenedores
 * - Seguimiento de contenedores (CU-02)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Crear un nuevo contenedor.
     * Valida que el número de serie sea único y que el cliente exista.
     *
     * @param request datos del contenedor
     * @return ContenedorResponse con los datos del contenedor creado
     * @throws IllegalArgumentException si el número de serie ya existe o el cliente no existe
     */
    @Transactional
    public ContenedorResponse crearContenedor(ContenedorRequest request) {
        log.info("Creando contenedor con número de serie: {}", request.getNumeroSerie());

        // Validar que el número de serie no exista
        if (contenedorRepository.existsByNumeroSerie(request.getNumeroSerie())) {
            throw new IllegalArgumentException(
                    "Ya existe un contenedor con el número de serie: " + request.getNumeroSerie());
        }

        // Validar que el cliente exista
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado con id: " + request.getClienteId()));

        Contenedor contenedor = ContenedorMapper.toEntity(request, cliente);
        Contenedor contenedorGuardado = contenedorRepository.save(contenedor);

        log.info("Contenedor creado con id: {}", contenedorGuardado.getId());
        return ContenedorMapper.toResponse(contenedorGuardado);
    }

    /**
     * Obtener un contenedor por su ID.
     *
     * @param id identificador del contenedor
     * @return ContenedorResponse con los datos del contenedor
     * @throws IllegalArgumentException si el contenedor no existe
     */
    @Transactional(readOnly = true)
    public ContenedorResponse obtenerContenedorPorId(Long id) {
        log.info("Consultando contenedor con id: {}", id);

        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contenedor no encontrado con id: " + id));

        return ContenedorMapper.toResponse(contenedor);
    }

    /**
     * Obtener un contenedor por su número de serie.
     *
     * @param numeroSerie número de serie del contenedor
     * @return ContenedorResponse con los datos del contenedor
     * @throws IllegalArgumentException si el contenedor no existe
     */
    @Transactional(readOnly = true)
    public ContenedorResponse obtenerContenedorPorNumeroSerie(String numeroSerie) {
        log.info("Consultando contenedor con número de serie: {}", numeroSerie);

        Contenedor contenedor = contenedorRepository.findByNumeroSerie(numeroSerie)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contenedor no encontrado con número de serie: " + numeroSerie));

        return ContenedorMapper.toResponse(contenedor);
    }

    /**
     * Obtener todos los contenedores del sistema.
     *
     * @return lista de todos los contenedores
     */
    @Transactional(readOnly = true)
    public List<ContenedorResponse> obtenerTodosLosContenedores() {
        log.info("Consultando todos los contenedores");

        return contenedorRepository.findAll().stream()
                .map(ContenedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los contenedores de un cliente específico.
     *
     * @param clienteId identificador del cliente
     * @return lista de contenedores del cliente
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Transactional(readOnly = true)
    public List<ContenedorResponse> obtenerContenedoresPorCliente(Long clienteId) {
        log.info("Consultando contenedores del cliente: {}", clienteId);

        // Validar que el cliente existe
        if (!clienteRepository.existsById(clienteId)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + clienteId);
        }

        return contenedorRepository.findByClienteId(clienteId).stream()
                .map(ContenedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener contenedores por estado.
     *
     * @param estado estado del contenedor
     * @return lista de contenedores con el estado especificado
     */
    @Transactional(readOnly = true)
    public List<ContenedorResponse> obtenerContenedoresPorEstado(EstadoContenedor estado) {
        log.info("Consultando contenedores con estado: {}", estado);

        return contenedorRepository.findByEstado(estado).stream()
                .map(ContenedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar datos de un contenedor existente.
     * No permite cambiar el cliente asociado ni el número de serie si ya está en uso.
     *
     * @param id identificador del contenedor a actualizar
     * @param request nuevos datos del contenedor
     * @return ContenedorResponse con los datos actualizados
     * @throws IllegalArgumentException si el contenedor no existe o el número de serie está en uso
     * @throws IllegalStateException si el contenedor ya está en tránsito
     */
    @Transactional
    public ContenedorResponse actualizarContenedor(Long id, ContenedorRequest request) {
        log.info("Actualizando contenedor con id: {}", id);

        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contenedor no encontrado con id: " + id));

        // No permitir actualizar si está en tránsito o entregado
        if (contenedor.getEstado() == EstadoContenedor.EN_TRASLADO
                || contenedor.getEstado() == EstadoContenedor.ENTREGADO) {
            throw new IllegalStateException(
                    "No se puede actualizar un contenedor en estado: " + contenedor.getEstado());
        }

        // Validar que el nuevo número de serie no esté en uso por otro contenedor
        if (!contenedor.getNumeroSerie().equals(request.getNumeroSerie())) {
            if (contenedorRepository.existsByNumeroSerie(request.getNumeroSerie())) {
                throw new IllegalArgumentException(
                        "El número de serie " + request.getNumeroSerie() + " ya está en uso");
            }
        }

        ContenedorMapper.updateEntity(contenedor, request);
        Contenedor contenedorActualizado = contenedorRepository.save(contenedor);

        log.info("Contenedor actualizado con id: {}", contenedorActualizado.getId());
        return ContenedorMapper.toResponse(contenedorActualizado);
    }

    /**
     * Cambiar el estado de un contenedor.
     * Valida las transiciones de estado permitidas.
     *
     * @param id identificador del contenedor
     * @param nuevoEstado nuevo estado del contenedor
     * @return ContenedorResponse con el estado actualizado
     * @throws IllegalArgumentException si el contenedor no existe
     * @throws IllegalStateException si la transición de estado no es válida
     */
    @Transactional
    public ContenedorResponse cambiarEstadoContenedor(Long id, EstadoContenedor nuevoEstado) {
        log.info("Cambiando estado del contenedor {} a {}", id, nuevoEstado);

        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contenedor no encontrado con id: " + id));

        // Validar transición de estado
        validarTransicionEstado(contenedor.getEstado(), nuevoEstado);

        contenedor.setEstado(nuevoEstado);
        Contenedor contenedorActualizado = contenedorRepository.save(contenedor);

        log.info("Estado del contenedor {} actualizado a {}", id, nuevoEstado);
        return ContenedorMapper.toResponse(contenedorActualizado);
    }

    /**
     * Eliminar un contenedor del sistema.
     * Solo puede eliminarse si está en estado REGISTRADO.
     *
     * @param id identificador del contenedor a eliminar
     * @throws IllegalArgumentException si el contenedor no existe
     * @throws IllegalStateException si el contenedor no puede ser eliminado
     */
    @Transactional
    public void eliminarContenedor(Long id) {
        log.info("Eliminando contenedor con id: {}", id);

        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contenedor no encontrado con id: " + id));

        // Solo permitir eliminar si está en estado REGISTRADO
        if (contenedor.getEstado() != EstadoContenedor.REGISTRADO) {
            throw new IllegalStateException(
                    "Solo se pueden eliminar contenedores en estado REGISTRADO. Estado actual: "
                            + contenedor.getEstado());
        }

        contenedorRepository.delete(contenedor);
        log.info("Contenedor eliminado con id: {}", id);
    }

    /**
     * Verificar si un contenedor puede ser asignado a una solicitud.
     * Debe estar en estado REGISTRADO o LISTO_PARA_RETIRO.
     *
     * @param id identificador del contenedor
     * @return true si puede ser asignado, false si no
     */
    @Transactional(readOnly = true)
    public boolean puedeSerAsignado(Long id) {
        Contenedor contenedor = contenedorRepository.findById(id)
                .orElse(null);

        if (contenedor == null) {
            return false;
        }

        return contenedor.getEstado() == EstadoContenedor.REGISTRADO
                || contenedor.getEstado() == EstadoContenedor.LISTO_PARA_RETIRO;
    }

    /**
     * Valida las transiciones de estado permitidas.
     * Reglas:
     * - REGISTRADO → LISTO_PARA_RETIRO, EN_TRASLADO
     * - LISTO_PARA_RETIRO → EN_TRASLADO
     * - EN_TRASLADO → EN_DEPOSITO, ENTREGADO
     * - EN_DEPOSITO → EN_TRASLADO
     * - ENTREGADO → (estado final, no puede cambiar)
     *
     * @param estadoActual estado actual del contenedor
     * @param nuevoEstado nuevo estado deseado
     * @throws IllegalStateException si la transición no es válida
     */
    private void validarTransicionEstado(EstadoContenedor estadoActual, EstadoContenedor nuevoEstado) {
        if (estadoActual == nuevoEstado) {
            return; // No hay transición
        }

        boolean transicionValida = switch (estadoActual) {
            case REGISTRADO -> nuevoEstado == EstadoContenedor.LISTO_PARA_RETIRO
                    || nuevoEstado == EstadoContenedor.EN_TRASLADO;
            case LISTO_PARA_RETIRO -> nuevoEstado == EstadoContenedor.EN_TRASLADO;
            case EN_TRASLADO -> nuevoEstado == EstadoContenedor.EN_DEPOSITO
                    || nuevoEstado == EstadoContenedor.ENTREGADO;
            case EN_DEPOSITO -> nuevoEstado == EstadoContenedor.EN_TRASLADO;
            case ENTREGADO -> false; // Estado final
        };

        if (!transicionValida) {
            throw new IllegalStateException(
                    String.format("Transición de estado inválida: %s → %s", estadoActual, nuevoEstado));
        }
    }
}
