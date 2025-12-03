package ar.edu.utn.frc.backend.tpi.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder,
                                     @Value("${tpi.gateway.solicitudes-service-url}") String solicitudesUrl,
                                     @Value("${tpi.gateway.flota-service-url}") String flotaUrl,
                                     @Value("${tpi.gateway.costos-service-url}") String costosUrl) {
        return builder.routes()
                .route("solicitudes-service", r -> r
                        .path("/api/solicitudes/**", "/api/clientes/**")
                        .uri(solicitudesUrl))
                .route("flota-service", r -> r
                        .path("/api/camiones/**", "/api/depositos/**")
                        .uri(flotaUrl))
                .route("costos-service", r -> r
                        .path("/api/tarifas/**")
                        .uri(costosUrl))
                .build();
    }
}
