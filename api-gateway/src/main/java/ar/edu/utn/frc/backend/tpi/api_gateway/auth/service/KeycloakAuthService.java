package ar.edu.utn.frc.backend.tpi.api_gateway.auth.service;

import ar.edu.utn.frc.backend.tpi.api_gateway.auth.KeycloakProperties;
import ar.edu.utn.frc.backend.tpi.api_gateway.auth.dto.KeycloakTokenResponse;
import ar.edu.utn.frc.backend.tpi.api_gateway.auth.dto.LoginRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class KeycloakAuthService {

    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;

    public KeycloakAuthService(RestTemplate restTemplate, KeycloakProperties keycloakProperties) {
        this.restTemplate = restTemplate;
        this.keycloakProperties = keycloakProperties;
    }

    public KeycloakTokenResponse login(LoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", keycloakProperties.getClientId());
        if (!keycloakProperties.getClientSecret().isBlank()) {
            form.add("client_secret", keycloakProperties.getClientSecret());
        }
        form.add("grant_type", "password");
        form.add("username", request.username());
        form.add("password", request.password());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    keycloakProperties.getTokenUrl(),
                    entity,
                    KeycloakTokenResponse.class
            );
            return Objects.requireNonNull(response.getBody(), "La respuesta de Keycloak no contiene cuerpo");
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
            if (status == HttpStatus.BAD_REQUEST || status == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al comunicarse con Keycloak", ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al comunicarse con Keycloak", ex);
        }
    }
}
