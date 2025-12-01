package ar.edu.utn.frc.backend.tpi.costos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistanceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.maps.api-key:}")
    private String apiKey;

    public DistanceResult calcularDistancia(Double origenLat, Double origenLng, Double destinoLat, Double destinoLng) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key de Google Maps no configurada");
        }
        String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%f,%f&destinations=%f,%f&key=%s",
                origenLat, origenLng, destinoLat, destinoLng, apiKey);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("No se pudo obtener distancia de Google Maps");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode row = root.path("rows").get(0);
            JsonNode element = row.path("elements").get(0);
            if (!"OK".equals(element.path("status").asText())) {
                throw new IllegalStateException("Respuesta de Google Maps no OK: " + element.path("status").asText());
            }

            double distanciaMetros = element.path("distance").path("value").asDouble();
            double duracionSegundos = element.path("duration").path("value").asDouble();

            return new DistanceResult(distanciaMetros / 1000.0, duracionSegundos / 3600.0);
        } catch (Exception e) {
            log.error("Error parseando respuesta de Google Maps", e);
            throw new IllegalStateException("Error parseando respuesta de Google Maps", e);
        }
    }

    public record DistanceResult(double distanciaKm, double duracionHoras) {
    }
}
