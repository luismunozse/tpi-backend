package ar.edu.utn.frc.backend.tpi.flota.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.flota.dto.DepositoDto;
import ar.edu.utn.frc.backend.tpi.flota.mapper.DepositoMapper;
import ar.edu.utn.frc.backend.tpi.flota.model.Deposito;
import ar.edu.utn.frc.backend.tpi.flota.repository.DepositoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositoService {

    private final DepositoRepository depositoRepository;

    @Transactional
    public DepositoDto crear(DepositoDto dto) {
        Deposito deposito = DepositoMapper.toEntity(dto);
        return DepositoMapper.toDto(depositoRepository.save(deposito));
    }

    @Transactional(readOnly = true)
    public List<DepositoDto> listarTodos() {
        return depositoRepository.findAll().stream()
                .map(DepositoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepositoDto obtener(Long id) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deposito no encontrado con id " + id));
        return DepositoMapper.toDto(deposito);
    }

    @Transactional
    public DepositoDto actualizar(Long id, DepositoDto dto) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deposito no encontrado con id " + id));

        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());
        deposito.setCostoEstadiaDiaria(dto.getCostoEstadiaDiaria());

        return DepositoMapper.toDto(depositoRepository.save(deposito));
    }

    @Transactional
    public void eliminar(Long id) {
        depositoRepository.deleteById(id);
    }
}