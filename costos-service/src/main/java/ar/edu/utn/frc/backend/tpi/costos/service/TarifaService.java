package ar.edu.utn.frc.backend.tpi.costos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.tpi.costos.dto.EstimacionCostoGeoRequest;
import ar.edu.utn.frc.backend.tpi.costos.dto.EstimacionCostoRequest;
import ar.edu.utn.frc.backend.tpi.costos.dto.EstimacionCostoResponse;
import ar.edu.utn.frc.backend.tpi.costos.dto.TarifaDto;
import ar.edu.utn.frc.backend.tpi.costos.mapper.TarifaMapper;
import ar.edu.utn.frc.backend.tpi.costos.model.Tarifa;
import ar.edu.utn.frc.backend.tpi.costos.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final TarifaRepository tarifaRepository;
    private final DistanceService distanceService;

    @Transactional
    public TarifaDto crear(TarifaDto dto) {
        Tarifa tarifa = TarifaMapper.toEntity(dto);
        return TarifaMapper.toDto(tarifaRepository.save(tarifa));
    }

    @Transactional(readOnly = true)
    public List<TarifaDto> listar() {
        return tarifaRepository.findAll().stream()
                .map(TarifaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TarifaDto obtener(Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con id " + id));
        return TarifaMapper.toDto(tarifa);
    }

    @Transactional
    public TarifaDto actualizar(Long id, TarifaDto dto) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con id " + id));

        tarifa.setNombre(dto.getNombre());
        tarifa.setCostoBaseKm(dto.getCostoBaseKm());
        tarifa.setValorCombustibleLitro(dto.getValorCombustibleLitro());
        tarifa.setCostoEstadiaDiaria(dto.getCostoEstadiaDiaria());
        tarifa.setVelocidadPromedioKmH(dto.getVelocidadPromedioKmH());

        return TarifaMapper.toDto(tarifaRepository.save(tarifa));
    }

    @Transactional
    public void eliminar(Long id) {
        tarifaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public EstimacionCostoResponse estimar(EstimacionCostoRequest request) {
        Tarifa tarifa = obtenerTarifa(request.getTarifaId());
        return calcularEstimacion(request.getDistanciaKm(), request.getCostoBaseKmCamion(),
                request.getConsumoCamionLitrosKm(), request.getDiasEstadia(), request.getCostoEstadiaDiaria(), tarifa);
    }

    @Transactional(readOnly = true)
    public EstimacionCostoResponse estimarConGeolocalizacion(EstimacionCostoGeoRequest request) {
        Tarifa tarifa = obtenerTarifa(request.getTarifaId());
        var distancia = distanceService.calcularDistancia(
                request.getOrigenLat(), request.getOrigenLng(), request.getDestinoLat(), request.getDestinoLng());
        return calcularEstimacion(distancia.distanciaKm(), request.getCostoBaseKmCamion(),
                request.getConsumoCamionLitrosKm(), request.getDiasEstadia(), request.getCostoEstadiaDiaria(), tarifa,
                distancia.duracionHoras());
    }

    private Tarifa obtenerTarifa(Long tarifaId) {
        if (tarifaId != null) {
            return tarifaRepository.findById(tarifaId)
                    .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con id " + tarifaId));
        }
        return tarifaRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay tarifas configuradas"));
    }

    private EstimacionCostoResponse calcularEstimacion(Double distanciaKm, Double costoBaseKmCamion,
            Double consumoCamionLitrosKm, Double diasEstadia, Double costoEstadiaDiaria, Tarifa tarifa) {
        return calcularEstimacion(distanciaKm, costoBaseKmCamion, consumoCamionLitrosKm, diasEstadia,
                costoEstadiaDiaria, tarifa, tarifa.getVelocidadPromedioKmH() > 0
                        ? distanciaKm / tarifa.getVelocidadPromedioKmH()
                        : 0.0);
    }

    private EstimacionCostoResponse calcularEstimacion(Double distanciaKm, Double costoBaseKmCamion,
            Double consumoCamionLitrosKm, Double diasEstadia, Double costoEstadiaDiaria, Tarifa tarifa,
            Double tiempoEstimadoHoras) {

        double costoKilometraje = distanciaKm * (tarifa.getCostoBaseKm() + costoBaseKmCamion);
        double costoCombustible = distanciaKm * consumoCamionLitrosKm * tarifa.getValorCombustibleLitro();
        double costoEstadia = diasEstadia * costoEstadiaDiaria;
        double costoTotal = costoKilometraje + costoCombustible + costoEstadia + tarifa.getCostoGestionFijo();

        return EstimacionCostoResponse.builder()
                .distanciaKm(distanciaKm)
                .costoKilometraje(costoKilometraje)
                .costoCombustible(costoCombustible)
                .costoEstadia(costoEstadia)
                .costoTotal(costoTotal)
                .tiempoEstimadoHoras(tiempoEstimadoHoras)
                .build();
    }
}
