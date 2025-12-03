# TPI Backend - Microservicios

Solución de logística de transporte de contenedores basada en microservicios. Incluye gateway, solicitudes, flota y costos, securizados con Keycloak (JWT).

## Descripción del proyecto
Backend para gestionar el traslado de contenedores: registro de solicitudes, asignación de rutas y camiones, cálculo de costos y tiempos, y seguimiento. Cada microservicio es independiente, con su base de datos y responsabilidades claras.

## Servicios
- **api-gateway**: enruta tráfico y relaya tokens (`/solicitudes/**`, `/flota/**`, `/costos/**`).
- **solicitudes-service**: gestión de solicitudes, rutas, tramos y clientes; controla estados del envío y validación por rol.
- **flota-service**: inventario de camiones (capacidad, disponibilidad, asignación/liberación) y depósitos.
- **costos-service**: tarifas base, cálculo de costos/tiempos (incluye distancia con Google Maps Distance Matrix) y estimaciones.
- Bases de datos Postgres por servicio y Keycloak en contenedor.

## Requisitos
- Docker y Docker Compose.
- Java 21 + Maven (si se compila localmente).
- Clave de Google Maps Distance Matrix (`GOOGLE_MAPS_API_KEY`).

## Variables de entorno
Definidas en `.env`:
- `KEYCLOAK_ISSUER_URI` (ej: `http://keycloak:8080/realms/tpi-backend` en compose).
- `GOOGLE_MAPS_API_KEY` (para costos-service).

## Puertos (host)
- Keycloak: 9090 → 8080
- Gateway: 8080
- Solicitudes: 8081
- Flota: 8082
- Costos: 8083
- Postgres: 5432 interno (uno por servicio)

## Levantar con Docker Compose
Desde la raíz:
```bash
docker compose up -d
```
Keycloak quedará accesible en `http://localhost:9090/` (admin/admin123 por defecto). Crear realm `tpi-backend`, roles `CLIENTE`, `ADMIN`, `TRANSPORTISTA`, usuarios de prueba y cliente `api-gateway` (confidencial con secret `gateway_secret`, redirect `http://localhost:8080/*`).

## Build local (opcional)
Linux/macOS:
```bash
cd api-gateway && mvn clean package -DskipTests && cd ..
cd solicitudes-service && mvn clean package -DskipTests && cd ..
cd flota-service && mvn clean package -DskipTests && cd ..
cd costos-service && mvn clean package -DskipTests && cd ..
```

Windows (PowerShell):
```powershell
cd api-gateway; mvn clean package -DskipTests; cd ..
cd solicitudes-service; mvn clean package -DskipTests; cd ..
cd flota-service; mvn clean package -DskipTests; cd ..
cd costos-service; mvn clean package -DskipTests; cd ..
```

## Swagger / OpenAPI
UI en cada servicio:
- `http://localhost:8081/swagger-ui.html` (solicitudes)
- `http://localhost:8082/swagger-ui.html` (flota)
- `http://localhost:8083/swagger-ui.html` (costos)
- JSON: `/v3/api-docs` en cada servicio.

## Roles y permisos (resumen)
- **CLIENTE**: crear y consultar sus solicitudes; estimar costos.
- **ADMIN**: gestión completa en todos los servicios.
- **TRANSPORTISTA**: consulta/asignación/liberación de camiones, registro de tramos.

## Flujos clave
- **Alta y estimación**: CLIENTE crea solicitud → Solicitudes valida cliente/contenedor → Costos estima distancia/costo (Google Maps) → respuesta con costo/tiempo.
- **Asignación y seguimiento**: ADMIN asigna ruta y camión disponible (Flota) acorde a peso/volumen → TRANSPORTISTA registra inicio/fin de tramo → Solicitud pasa a ENTREGADA y registra costo/tiempo real.

## Notas
- Configuración de seguridad: cada servicio expone `/actuator/health` y `/actuator/info` sin auth; resto protegido por JWT.
- Para cálculo de costos con geolocalización se usa Google Distance Matrix; asegúrate de tener la API key habilitada.

## Errores 
- **A El Registro "Atómico" de la Solicitud (RF 1a y 1b)**
Requerimiento: "La solicitud incluye la creación del contenedor... e incluye el registro del cliente si no existe previamente".
- Tu código actual: En SolicitudService.crearSolicitud, recibes clienteId y contenedorId. Si no existen en la base de datos, lanzas un error:
- El problema: El usuario tiene que hacer 3 llamadas (Crear Cliente -> Crear Contenedor -> Crear Solicitud). El enunciado pide que sea una sola llamada.

- **B. Cálculo de Estadía en Depósito (RF "Calcular costo total... c. Estadía")**
Requerimiento: "Estadía en depósitos (calculada a partir de la diferencia entre fechas reales de entrada y salida)".

Tu código actual: En SolicitudService.finalizarSolicitud, sumas el costoReal de los tramos. Pero, ¿quién calcula ese costo real de estadía?

Tienes costoEstadiaDiaria en Deposito y en Tarifa.

Pero al finalizar un tramo (registrarFinTramo), solo guardas la fecha. No veo que estés calculando automáticamente: (FechaSalida - FechaEntrada) * CostoDia.

Riesgo: Si el profesor finaliza un tramo y el costo sigue siendo "0" o "null", preguntará dónde está la lógica de negocio.