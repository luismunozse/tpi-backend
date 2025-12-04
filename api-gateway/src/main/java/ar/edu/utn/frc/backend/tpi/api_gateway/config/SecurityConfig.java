package ar.edu.utn.frc.backend.tpi.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Configuración reactiva para Spring Cloud Gateway.
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        // Endpoints públicos
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Tramos: TRANSPORTISTA puede consultar (GET) y registrar inicio/fin (POST)
                        .pathMatchers(HttpMethod.GET, "/solicitudes/tramos/**")
                        .hasAnyAuthority("TRANSPORTISTA", "ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.POST, "/solicitudes/tramos/*/inicio", "/solicitudes/tramos/*/fin")
                        .hasAnyAuthority("TRANSPORTISTA", "ADMIN")

                        // Solicitudes y clientes: CLIENTE puede crear/consultar, ADMIN gestión completa
                        .pathMatchers("/solicitudes/**", "/clientes/**")
                        .hasAnyAuthority("CLIENTE", "ADMIN")

                        // Flota: ADMIN y TRANSPORTISTA (RF6: asignar camión)
                        .pathMatchers("/flota/camiones/**", "/flota/depositos/**")
                        .hasAnyAuthority("ADMIN", "TRANSPORTISTA")

                        // Costos: ADMIN y CLIENTE (RF estimaciones)
                        .pathMatchers("/costos/**")
                        .hasAnyAuthority("ADMIN", "CLIENTE")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        // Convierte roles del JWT a GrantedAuthority para Spring Security (reactivo).
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
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
