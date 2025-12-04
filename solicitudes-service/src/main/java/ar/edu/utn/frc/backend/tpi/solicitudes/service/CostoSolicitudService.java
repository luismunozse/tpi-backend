package ar.edu.utn.frc.backend.tpi.solicitudes.service;

import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Solicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Tramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.TipoTramo;
import ar.edu.utn.frc.backend.tpi.solicitudes.repository.SolicitudRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para calcular el costo total de una solicitud de transporte.
 *
 * El cálculo incluye:
 * 1. Costo por recorrido total (distancia de todos los tramos)
 * 2. Costo por peso y volumen del contenedor
 * 3. Costo por estadía en depósitos (diferencia entre fechas de entrada y salida)
 *
 * Implementa requisito: Calcular el costo total de la entrega.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostoSolicitudService {

    private final SolicitudRepository solicitudRepository;

    // Constantes para el cálculo de costos
    private static final Double COSTO_BASE_POR_KM = 50.0; // Pesos por kilómetro
    private static final Double COSTO_POR_TONELADA = 1000.0; // Pesos por tonelada
    private static final Double COSTO_POR_M3 = 500.0; // Pesos por metro cúbico
    private static final Double COSTO_ESTADIA_POR_DIA = 2000.0; // Pesos por día en depósito

    /**
     * Calcula el costo total de una solicitud basándose en:
     * - Recorrido total (suma de distancias de tramos o estimaciones de ruta)
     * - Peso y volumen del contenedor
     * - Estadía en depósitos (tiempos reales si están disponibles)
     *
     * @param solicitudId identificador de la solicitud
     * @return detalle completo de costo total
     * @throws IllegalArgumentException si la solicitud no existe o no tiene ruta asignada
     */
    @Transactional(readOnly = true)
    public DesgloseCostoTotal calcularCostoTotal(Long solicitudId) {
        log.info("Calculando costo total para solicitud {}", solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud no encontrada con id: " + solicitudId));

        if (solicitud.getRuta() == null) {
            throw new IllegalArgumentException(
                    "La solicitud " + solicitudId + " no tiene una ruta asignada");
        }

        Contenedor contenedor = solicitud.getContenedor();
        List<Tramo> tramos = solicitud.getRuta().getTramos();

        // 1. Calcular costo por recorrido
        double costoRecorrido = calcularCostoRecorrido(tramos);

        // 2. Calcular costo por peso y volumen del contenedor
        double costoPesoVolumen = calcularCostoPesoVolumen(contenedor);

        // 3. Calcular costo por estadía en depósitos
        double costoEstadia = calcularCostoEstadia(tramos);

        // 4. Costo total
        double costoTotal = costoRecorrido + costoPesoVolumen + costoEstadia;

        log.info("Costo total calculado para solicitud {}: ${}", solicitudId, costoTotal);

        return DesgloseCostoTotal.builder()
                .solicitudId(solicitudId)
                .costoRecorrido(costoRecorrido)
                .distanciaTotalKm(solicitud.getRuta().getDistanciaTotalKm())
                .costoPesoVolumen(costoPesoVolumen)
                .pesoContenedorKg(contenedor.getPeso())
                .volumenContenedorM3(contenedor.getVolumen())
                .costoEstadia(costoEstadia)
                .diasEstadia(calcularDiasEstadia(tramos))
                .costoTotal(costoTotal)
                .usaCostosReales(tramos.stream().allMatch(t -> t.getCostoReal() != null))
                .build();
    }

    /**
     * Calcula el costo del recorrido total.
     * Usa costos reales de los tramos si están disponibles, o estimados si no.
     *
     * @param tramos lista de tramos de la ruta
     * @return costo total del recorrido
     */
    private double calcularCostoRecorrido(List<Tramo> tramos) {
        // Priorizar costos reales si están disponibles
        boolean todosTienenCostoReal = tramos.stream()
                .allMatch(t -> t.getCostoReal() != null);

        if (todosTienenCostoReal) {
            log.info("Usando costos reales de tramos");
            return tramos.stream()
                    .mapToDouble(Tramo::getCostoReal)
                    .sum();
        }

        // Si no hay costos reales, usar estimados
        boolean todosTienenCostoEstimado = tramos.stream()
                .allMatch(t -> t.getCostoEstimado() != null);

        if (todosTienenCostoEstimado) {
            log.info("Usando costos estimados de tramos");
            return tramos.stream()
                    .mapToDouble(Tramo::getCostoEstimado)
                    .sum();
        }

        // Si no hay costos, usar cálculo básico
        log.warn("No hay costos disponibles, usando cálculo básico");
        return 0.0; // El cálculo básico requeriría conocer distancias individuales
    }

    /**
     * Calcula el costo basado en peso y volumen del contenedor.
     * Aplica factores de costo por tonelada y por metro cúbico.
     *
     * @param contenedor contenedor a transportar
     * @return costo por características físicas
     */
    private double calcularCostoPesoVolumen(Contenedor contenedor) {
        double pesoToneladas = contenedor.getPeso() / 1000.0; // Convertir kg a toneladas
        double costoPorPeso = pesoToneladas * COSTO_POR_TONELADA;
        double costoPorVolumen = contenedor.getVolumen() * COSTO_POR_M3;

        log.debug("Costo por peso ({}t): ${}, Costo por volumen ({}m³): ${}",
                pesoToneladas, costoPorPeso, contenedor.getVolumen(), costoPorVolumen);

        return costoPorPeso + costoPorVolumen;
    }

    /**
     * Calcula el costo de estadía en depósitos.
     *
     * Se basa en la diferencia de tiempo entre:
     * - Fin del tramo que llega al depósito (entrada)
     * - Inicio del tramo que sale del depósito (salida)
     *
     * Solo considera tramos de tipo DEPOSITO_* que hayan sido ejecutados.
     *
     * @param tramos lista de tramos de la ruta
     * @return costo total de estadía en depósitos
     */
    private double calcularCostoEstadia(List<Tramo> tramos) {
        double costoTotal = 0.0;

        for (int i = 0; i < tramos.size() - 1; i++) {
            Tramo tramoActual = tramos.get(i);
            Tramo tramoSiguiente = tramos.get(i + 1);

            // Verificar si hay estadía en depósito
            // (el tramo actual termina en depósito y el siguiente sale de depósito)
            boolean terminaEnDeposito = tramoActual.getTipo() == TipoTramo.ORIGEN_DEPOSITO
                    || tramoActual.getTipo() == TipoTramo.DEPOSITO_DEPOSITO;

            boolean siguienteSaleDeDeposito = tramoSiguiente.getTipo() == TipoTramo.DEPOSITO_DEPOSITO
                    || tramoSiguiente.getTipo() == TipoTramo.DEPOSITO_DESTINO;

            if (terminaEnDeposito && siguienteSaleDeDeposito
                    && tramoActual.getFechaHoraFin() != null
                    && tramoSiguiente.getFechaHoraInicio() != null) {

                LocalDateTime entrada = tramoActual.getFechaHoraFin();
                LocalDateTime salida = tramoSiguiente.getFechaHoraInicio();

                Duration duracion = Duration.between(entrada, salida);
                double dias = duracion.toHours() / 24.0;

                double costoEstadia = dias * COSTO_ESTADIA_POR_DIA;
                costoTotal += costoEstadia;

                log.debug("Estadía en depósito: {} días, costo: ${}", dias, costoEstadia);
            }
        }

        return costoTotal;
    }

    /**
     * Calcula los días totales de estadía en depósitos.
     *
     * @param tramos lista de tramos
     * @return días totales de estadía
     */
    private double calcularDiasEstadia(List<Tramo> tramos) {
        double diasTotal = 0.0;

        for (int i = 0; i < tramos.size() - 1; i++) {
            Tramo tramoActual = tramos.get(i);
            Tramo tramoSiguiente = tramos.get(i + 1);

            boolean terminaEnDeposito = tramoActual.getTipo() == TipoTramo.ORIGEN_DEPOSITO
                    || tramoActual.getTipo() == TipoTramo.DEPOSITO_DEPOSITO;

            boolean siguienteSaleDeDeposito = tramoSiguiente.getTipo() == TipoTramo.DEPOSITO_DEPOSITO
                    || tramoSiguiente.getTipo() == TipoTramo.DEPOSITO_DESTINO;

            if (terminaEnDeposito && siguienteSaleDeDeposito
                    && tramoActual.getFechaHoraFin() != null
                    && tramoSiguiente.getFechaHoraInicio() != null) {

                LocalDateTime entrada = tramoActual.getFechaHoraFin();
                LocalDateTime salida = tramoSiguiente.getFechaHoraInicio();

                Duration duracion = Duration.between(entrada, salida);
                diasTotal += duracion.toHours() / 24.0;
            }
        }

        return diasTotal;
    }

    /**
     * DTO que representa el desglose completo del costo de una solicitud.
     */
    @Data
    @Builder
    public static class DesgloseCostoTotal {
        private Long solicitudId;

        // Costo por recorrido
        private Double costoRecorrido;
        private Double distanciaTotalKm;

        // Costo por peso y volumen
        private Double costoPesoVolumen;
        private Double pesoContenedorKg;
        private Double volumenContenedorM3;

        // Costo por estadía
        private Double costoEstadia;
        private Double diasEstadia;

        // Total
        private Double costoTotal;

        // Metadata
        private Boolean usaCostosReales; // true si usa costos reales, false si usa estimados
    }
}
