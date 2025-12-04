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
                        .path("/solicitudes/**", "/clientes/**")
                        .filters(f -> f
                                // Soporta /solicitudes y /solicitudes/... (segmento opcional)
                                .rewritePath("/solicitudes(?<segment>/.*)?", "/api/solicitudes${segment}")
                                // Soporta /clientes y /clientes/...
                                .rewritePath("/clientes(?<segment>/.*)?", "/api/clientes${segment}")
                        )
                        .uri(solicitudesUrl))
                .route("contenedores", r -> r
                        .path("/contenedores/**")
                        .filters(f -> f.rewritePath("/contenedores(?<segment>/.*)?", "/api/contenedores${segment}"))
                        .uri(solicitudesUrl))
                .route("tramos", r -> r
                        .path("/tramos/**")
                        .filters(f -> f.rewritePath("/tramos(?<segment>/.*)?", "/api/tramos${segment}"))
                        .uri(solicitudesUrl))
                .route("rutas", r -> r
                        .path("/rutas/**")
                        .filters(f -> f.rewritePath("/rutas(?<segment>/.*)?", "/api/rutas${segment}"))
                        .uri(solicitudesUrl))
                .route("flota-service", r -> r
                        .path("/flota/camiones/**", "/flota/depositos/**")
                        .filters(f -> f
                                .rewritePath("/flota/camiones(?<segment>/.*)?", "/api/camiones${segment}")
                                .rewritePath("/flota/depositos(?<segment>/.*)?", "/api/depositos${segment}")
                        )
                        .uri(flotaUrl))
                .route("costos-service", r -> r
                        .path("/costos/**")
                        .filters(f -> f.rewritePath("/costos(?<segment>/.*)?", "/api/tarifas${segment}"))
                        .uri(costosUrl))
                .build();
    }
}
