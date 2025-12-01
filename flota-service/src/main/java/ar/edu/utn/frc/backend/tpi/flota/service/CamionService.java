package ar.edu.utn.frc.backend.tpi.flota.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.flota.dto.CamionDto;
import ar.edu.utn.frc.backend.tpi.flota.dto.AsignacionCamionRequest;
import ar.edu.utn.frc.backend.tpi.flota.mapper.CamionMapper;
import ar.edu.utn.frc.backend.tpi.flota.model.Camion;
import ar.edu.utn.frc.backend.tpi.flota.model.EstadoCamion;
import ar.edu.utn.frc.backend.tpi.flota.repository.CamionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CamionService {

    private final CamionRepository camionRepository;

    @Transactional
    public CamionDto crear(CamionDto dto) {
        Camion camion = CamionMapper.toEntity(dto);
        camion.setEstado(camion.getEstado() != null ? camion.getEstado() : EstadoCamion.DISPONIBLE);
        return CamionMapper.toDto(camionRepository.save(camion));
    }

    @Transactional(readOnly = true)
    public List<CamionDto> listarTodos() {
        return camionRepository.findAll().stream()
                .map(CamionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CamionDto> listarDisponibles() {
        return listarPorEstado(EstadoCamion.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public List<CamionDto> listarPorEstado(EstadoCamion estado) {
        return camionRepository.findByEstado(estado).stream()
                .map(CamionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CamionDto obtener(Long id) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Camion no encontrado con id " + id));
        return CamionMapper.toDto(camion);
    }

    @Transactional
    public CamionDto actualizar(Long id, CamionDto dto) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Camion no encontrado con id " + id));

        camion.setDominio(dto.getDominio());
        camion.setNombreTransportista(dto.getNombreTransportista());
        camion.setTelefono(dto.getTelefono());
        camion.setCapacidadPesoKg(dto.getCapacidadPesoKg());
        camion.setCapacidadVolumenM3(dto.getCapacidadVolumenM3());
        camion.setConsumoCombustibleLitrosKm(dto.getConsumoCombustibleLitrosKm());
        camion.setCostoBaseKm(dto.getCostoBaseKm());
        if (dto.getEstado() != null) {
            camion.setEstado(dto.getEstado());
        }

        return CamionMapper.toDto(camionRepository.save(camion));
    }

    @Transactional
    public void eliminar(Long id) {
        camionRepository.deleteById(id);
    }

    @Transactional
    public CamionDto asignar(Long id, AsignacionCamionRequest request) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Camion no encontrado con id " + id));

        if (camion.getEstado() != EstadoCamion.DISPONIBLE) {
            throw new IllegalStateException("El camión no está disponible");
        }
        if (request.getPesoKg() > camion.getCapacidadPesoKg()
                || request.getVolumenM3() > camion.getCapacidadVolumenM3()) {
            throw new IllegalArgumentException("El camión no soporta peso/volumen solicitado");
        }

        camion.setEstado(EstadoCamion.OCUPADO);
        return CamionMapper.toDto(camionRepository.save(camion));
    }

    @Transactional
    public CamionDto liberar(Long id) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Camion no encontrado con id " + id));
        camion.setEstado(EstadoCamion.DISPONIBLE);
        return CamionMapper.toDto(camionRepository.save(camion));
    }

    @Transactional(readOnly = true)
    public List<CamionDto> disponiblesPorCapacidad(Double pesoKg, Double volumenM3) {
        return camionRepository.findByEstado(EstadoCamion.DISPONIBLE).stream()
                .filter(c -> (pesoKg == null || c.getCapacidadPesoKg() >= pesoKg)
                        && (volumenM3 == null || c.getCapacidadVolumenM3() >= volumenM3))
                .map(CamionMapper::toDto)
                .toList();
    }
}
