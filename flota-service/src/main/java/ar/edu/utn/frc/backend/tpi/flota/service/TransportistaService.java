package ar.edu.utn.frc.backend.tpi.flota.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.flota.dto.TransportistaDto;
import ar.edu.utn.frc.backend.tpi.flota.mapper.TransportistaMapper;
import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;
import ar.edu.utn.frc.backend.tpi.flota.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;

    @Transactional
    public TransportistaDto crear(TransportistaDto dto) {
        validarUnicidad(dto, null);
        Transportista transportista = TransportistaMapper.toEntity(dto);
        return TransportistaMapper.toDto(transportistaRepository.save(transportista));
    }

    @Transactional(readOnly = true)
    public List<TransportistaDto> listarTodos() {
        return transportistaRepository.findAll().stream()
                .map(TransportistaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransportistaDto obtener(Long id) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con id " + id));
        return TransportistaMapper.toDto(transportista);
    }

    @Transactional
    public TransportistaDto actualizar(Long id, TransportistaDto dto) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con id " + id));

        validarUnicidad(dto, id);

        transportista.setNombre(dto.getNombre());
        transportista.setApellido(dto.getApellido());
        transportista.setDni(dto.getDni());
        transportista.setEmail(dto.getEmail());
        transportista.setTelefono(dto.getTelefono());
        transportista.setNumeroLicencia(dto.getNumeroLicencia());
        transportista.setCategoriaLicencia(dto.getCategoriaLicencia());
        transportista.setActivo(Boolean.TRUE.equals(dto.getActivo()));

        return TransportistaMapper.toDto(transportistaRepository.save(transportista));
    }

    @Transactional
    public void eliminar(Long id) {
        transportistaRepository.deleteById(id);
    }

    private void validarUnicidad(TransportistaDto dto, Long idActual) {
        transportistaRepository.findByDni(dto.getDni()).ifPresent(t -> {
            if (idActual == null || !t.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un transportista con el DNI indicado");
            }
        });
        transportistaRepository.findByEmail(dto.getEmail()).ifPresent(t -> {
            if (idActual == null || !t.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un transportista con el email indicado");
            }
        });
        transportistaRepository.findByNumeroLicencia(dto.getNumeroLicencia()).ifPresent(t -> {
            if (idActual == null || !t.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un transportista con el numero de licencia indicado");
            }
        });
    }
}
