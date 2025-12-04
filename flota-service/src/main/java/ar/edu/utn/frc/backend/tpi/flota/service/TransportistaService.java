package ar.edu.utn.frc.backend.tpi.flota.service;

import ar.edu.utn.frc.backend.tpi.flota.dto.TransportistaDto;
import ar.edu.utn.frc.backend.tpi.flota.mapper.TransportistaMapper;
import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;
import ar.edu.utn.frc.backend.tpi.flota.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de transportistas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;

    /**
     * Crea un nuevo transportista.
     */
    @Transactional
    public TransportistaDto crear(TransportistaDto dto) {
        log.info("Creando transportista con DNI: {}", dto.getDni());

        // Validar que no exista un transportista con el mismo DNI
        if (transportistaRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un transportista con el DNI: " + dto.getDni());
        }

        // Validar que no exista un transportista con el mismo email
        if (transportistaRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un transportista con el email: " + dto.getEmail());
        }

        // Validar que no exista un transportista con el mismo número de licencia
        if (transportistaRepository.existsByNumeroLicencia(dto.getNumeroLicencia())) {
            throw new IllegalArgumentException("Ya existe un transportista con el número de licencia: " + dto.getNumeroLicencia());
        }

        Transportista transportista = TransportistaMapper.toEntity(dto);
        Transportista transportistaGuardado = transportistaRepository.save(transportista);

        log.info("Transportista creado con ID: {}", transportistaGuardado.getId());
        return TransportistaMapper.toDto(transportistaGuardado);
    }

    /**
     * Obtiene un transportista por su ID.
     */
    @Transactional(readOnly = true)
    public TransportistaDto obtenerPorId(Long id) {
        log.info("Obteniendo transportista con ID: {}", id);

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con ID: " + id));

        return TransportistaMapper.toDto(transportista);
    }

    /**
     * Obtiene un transportista por su DNI.
     */
    @Transactional(readOnly = true)
    public TransportistaDto obtenerPorDni(String dni) {
        log.info("Obteniendo transportista con DNI: {}", dni);

        Transportista transportista = transportistaRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con DNI: " + dni));

        return TransportistaMapper.toDto(transportista);
    }

    /**
     * Obtiene todos los transportistas.
     */
    @Transactional(readOnly = true)
    public List<TransportistaDto> listar() {
        log.info("Listando todos los transportistas");

        return transportistaRepository.findAll().stream()
                .map(TransportistaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los transportistas activos o inactivos.
     */
    @Transactional(readOnly = true)
    public List<TransportistaDto> listarPorEstado(Boolean activo) {
        log.info("Listando transportistas con estado activo: {}", activo);

        return transportistaRepository.findByActivo(activo).stream()
                .map(TransportistaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un transportista existente.
     */
    @Transactional
    public TransportistaDto actualizar(Long id, TransportistaDto dto) {
        log.info("Actualizando transportista con ID: {}", id);

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con ID: " + id));

        // Validar que no exista otro transportista con el mismo DNI
        if (!transportista.getDni().equals(dto.getDni()) && transportistaRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe otro transportista con el DNI: " + dto.getDni());
        }

        // Validar que no exista otro transportista con el mismo email
        if (!transportista.getEmail().equals(dto.getEmail()) && transportistaRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe otro transportista con el email: " + dto.getEmail());
        }

        // Validar que no exista otro transportista con el mismo número de licencia
        if (!transportista.getNumeroLicencia().equals(dto.getNumeroLicencia()) &&
                transportistaRepository.existsByNumeroLicencia(dto.getNumeroLicencia())) {
            throw new IllegalArgumentException("Ya existe otro transportista con el número de licencia: " + dto.getNumeroLicencia());
        }

        TransportistaMapper.updateEntity(transportista, dto);
        Transportista transportistaActualizado = transportistaRepository.save(transportista);

        log.info("Transportista actualizado con ID: {}", transportistaActualizado.getId());
        return TransportistaMapper.toDto(transportistaActualizado);
    }

    /**
     * Desactiva un transportista (soft delete).
     */
    @Transactional
    public TransportistaDto desactivar(Long id) {
        log.info("Desactivando transportista con ID: {}", id);

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con ID: " + id));

        transportista.setActivo(false);
        Transportista transportistaActualizado = transportistaRepository.save(transportista);

        log.info("Transportista desactivado con ID: {}", id);
        return TransportistaMapper.toDto(transportistaActualizado);
    }

    /**
     * Activa un transportista.
     */
    @Transactional
    public TransportistaDto activar(Long id) {
        log.info("Activando transportista con ID: {}", id);

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con ID: " + id));

        transportista.setActivo(true);
        Transportista transportistaActualizado = transportistaRepository.save(transportista);

        log.info("Transportista activado con ID: {}", id);
        return TransportistaMapper.toDto(transportistaActualizado);
    }

    /**
     * Elimina físicamente un transportista del sistema.
     * Solo puede eliminarse si no tiene camiones asignados.
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando transportista con ID: {}", id);

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con ID: " + id));

        // Validar que no tenga camiones asignados
        if (transportista.getCamiones() != null && !transportista.getCamiones().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un transportista con camiones asignados. " +
                    "Reasigne los camiones primero.");
        }

        transportistaRepository.delete(transportista);
        log.info("Transportista eliminado con ID: {}", id);
    }
}
