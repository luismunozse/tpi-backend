package ar.edu.utn.frc.backend.tpi.solicitudes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente interno para pedir al gateway que aprovisione usuarios en Keycloak.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayKeycloakClient {

    @Value("${gateway.internal.url:http://api-gateway:8080}")
    private String gatewayInternalUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void ensureClientUser(String nombre, String email) {
        String url = gatewayInternalUrl + "/internal/keycloak/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("nombre", nombre, "email", email);
        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
            log.info("Solicitada creación de usuario {} en Keycloak vía gateway", email);
        } catch (RestClientException ex) {
            log.warn("No se pudo solicitar creación de usuario {} en Keycloak: {}", email, ex.getMessage());
        }
    }
}
