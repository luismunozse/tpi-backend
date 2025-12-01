package ar.edu.utn.frc.backend.tpi.solicitudes.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura el servicio como Resource Server protegido por Keycloak.
 * Valida JWT, mapea roles y expone health/info sin autenticacion.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// Config de seguridad para actuar como Resource Server con JWT emitidos por Keycloak.
public class ResourceServerConfig {

    // Cadena de filtros HTTP: deshabilita CSRF, expone health/info sin auth y exige JWT al resto.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita CSRF ya que es un API REST.
            .csrf(csrf -> csrf.disable())
            // Configura reglas de autorizacion.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated())
                // Configura Resource Server para validar JWT.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        // Construye la cadena de filtros.
        return http.build();
    }

    /**
     * Convierte roles de realm_access.roles a autoridades ROLE_<ROL>.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Convierte scopes a autoridades ROLE_<SCOPE>.
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        // Convierte roles de realm_access a autoridades ROLE_<ROL>.
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Combina ambas conversiones.
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Incluye scopes y roles del realm en el JWT emitido por Keycloak.
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopesConverter.convert(jwt));
            // Extrae roles del realm_access.
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                // Convierte roles a autoridades ROLE_<ROL>.
                Object roles = realmAccess.get("roles");
                if (roles instanceof java.util.Collection<?> roleList) {
                    // Convierte roles a autoridades ROLE_<ROL>.
                    roleList.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .forEach(authorities::add);
                }
            }
            // Devuelve autoridades. 
            return authorities;
        });
        // Devuelve el convertidor.
        return converter;
    }
}
