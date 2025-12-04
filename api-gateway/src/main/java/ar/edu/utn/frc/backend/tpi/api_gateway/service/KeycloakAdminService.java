package ar.edu.utn.frc.backend.tpi.api_gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    @Value("${keycloak.admin.url:http://keycloak:8080}")
    private String keycloakUrl;
    @Value("${keycloak.admin.realm:tpi-backend}")
    private String realm;
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;
    @Value("${keycloak.admin.password:admin123}")
    private String adminPassword;
    @Value("${keycloak.admin.default-user-password:Temporal123!}")
    private String defaultUserPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    public void ensureClientUser(String nombre, String email) {
        try {
            String token = obtenerAdminToken();
            if (token == null) {
                log.warn("No se pudo obtener token admin de Keycloak; se omite creación de usuario {}", email);
                return;
            }
            if (existeUsuario(token, email)) {
                log.info("Usuario {} ya existe en Keycloak", email);
                return;
            }
            String userId = crearUsuario(token, nombre, email);
            if (userId != null) {
                asignarRolCliente(token, userId);
                log.info("Usuario {} creado en Keycloak con id {}", email, userId);
            }
        } catch (RestClientException ex) {
            log.warn("Error al sincronizar usuario {} en Keycloak: {}", email, ex.getMessage());
        }
    }

    private String obtenerAdminToken() {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        body.add("grant_type", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
        return response != null ? (String) response.get("access_token") : null;
    }

    private boolean existeUsuario(String token, String email) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users?username=" + email;
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        return resp.getBody() != null && !resp.getBody().isEmpty();
    }

    private String crearUsuario(String token, String nombre, String email) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";
        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of(
                "username", email,
                "email", email,
                "firstName", nombre,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", defaultUserPassword,
                        "temporary", true
                ))
        );
        ResponseEntity<Void> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, headers), Void.class);
        if (resp.getStatusCode().is2xxSuccessful()) {
            String location = resp.getHeaders().getFirst(HttpHeaders.LOCATION);
            if (location != null && location.contains("/users/")) {
                return location.substring(location.lastIndexOf('/') + 1);
            }
        }
        return null;
    }

    private void asignarRolCliente(String token, String userId) {
        String roleUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/CLIENTE";
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<Map> roleResp = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> role = roleResp.getBody();
        if (role == null) {
            log.warn("Rol CLIENTE no encontrado en Keycloak");
            return;
        }
        String assignUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        restTemplate.exchange(assignUrl, HttpMethod.POST, new HttpEntity<>(List.of(role), headers), Void.class);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
