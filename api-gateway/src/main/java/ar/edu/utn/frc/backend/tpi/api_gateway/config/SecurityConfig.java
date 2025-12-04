package ar.edu.utn.frc.backend.tpi.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Define reglas de acceso por path y roles; valida JWT emitidos por Keycloak.
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Tramos: TRANSPORTISTA puede consultar (GET) y registrar inicio/fin (POST)
                        .requestMatchers(HttpMethod.GET, "/solicitudes/tramos/**")
                        .hasAnyAuthority("TRANSPORTISTA", "ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/solicitudes/tramos/*/inicio", "/solicitudes/tramos/*/fin")
                        .hasAnyAuthority("TRANSPORTISTA", "ADMIN")

                        // Solicitudes y clientes: CLIENTE puede crear/consultar, ADMIN gestión completa
                        .requestMatchers("/solicitudes/**", "/clientes/**")
                        .hasAnyAuthority("CLIENTE", "ADMIN")

                        // Flota: ADMIN y TRANSPORTISTA (RF6: asignar camión)
                        .requestMatchers("/flota/camiones/**", "/flota/depositos/**")
                        .hasAnyAuthority("ADMIN", "TRANSPORTISTA")

                        // Costos: ADMIN y CLIENTE (RF estimaciones)
                        .requestMatchers("/costos/**")
                        .hasAnyAuthority("ADMIN", "CLIENTE")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Convierte roles del JWT a GrantedAuthority para Spring Security.
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    // Extrae y convierte roles del JWT a GrantedAuthority.
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return parseRoles(jwt).stream()
                .map(String::toUpperCase)
                .map(SimpleGrantedAuthority::new)
                .map(authority -> (GrantedAuthority) authority)
                .collect(Collectors.toList());
    }

    // Parsea roles desde realm_access y resource_access en el JWT.
    private Collection<String> parseRoles(Jwt jwt) {
        List<String> roles = new ArrayList<>();
        // Extrae roles del realm_access 
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object realmRoles = realmAccess.get("roles");
            if (realmRoles instanceof Collection<?> list) {
                list.stream().filter(String.class::isInstance).map(String.class::cast).forEach(roles::add);
            }
        }
        // Extrae roles del resource_access 
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
