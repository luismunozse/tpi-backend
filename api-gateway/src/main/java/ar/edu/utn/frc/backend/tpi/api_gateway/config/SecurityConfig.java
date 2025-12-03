package ar.edu.utn.frc.backend.tpi.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class SecurityConfig {

    // Configura la seguridad del API Gateway como Resource Server con JWT emitidos por Keycloak.
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) { 
        return http // Configuración de seguridad HTTP
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Deshabilita CSRF para APIs REST 
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/solicitudes/**", "/api/clientes/**")
                        .hasAnyRole("CLIENTE", "ADMIN", "TRANSPORTISTA")
                        .pathMatchers("/api/camiones/**", "/api/depositos/**")
                        .hasAnyRole("ADMIN", "TRANSPORTISTA")
                        .pathMatchers("/api/tarifas/**")
                        .hasAnyRole("ADMIN", "CLIENTE")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .oauth2Login(oauth2 -> {})
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Flux<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return Flux.fromIterable(parseRoles(jwt))
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new);
    }

    private Collection<String> parseRoles(Jwt jwt) {
        List<String> roles = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object realmRoles = realmAccess.get("roles");
            if (realmRoles instanceof Collection<?> list) {
                list.stream().filter(String.class::isInstance).map(String.class::cast).forEach(roles::add);
            }
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object clientEntry : resourceAccess.values()) {
                if (clientEntry instanceof Map<?, ?> clientMap) {
                    Object clientRoles = clientMap.get("roles");
                    if (clientRoles instanceof Collection<?> list) {
                        list.stream().filter(String.class::isInstance).map(String.class::cast).forEach(roles::add);
                    }
                }
            }
        }

        return Set.copyOf(roles);
    }
}
