# Colección Postman - TPI Backend

Esta carpeta contiene la colección de Postman lista para probar el sistema de gestión de transporte de contenedores.

## 📦 Archivos

- `TPI-Backend.postman_collection.json` - Colección con todos los endpoints
- `TPI-Backend.postman_environment.json` - Variables de entorno configuradas
- `README.md` - Este archivo

## 🚀 Cómo Importar

### 1. Importar la Colección

1. Abre Postman
2. Click en **Import** (esquina superior izquierda)
3. Arrastra el archivo `TPI-Backend.postman_collection.json` o haz click en **Upload Files**
4. Click en **Import**

### 2. Importar el Environment

1. En Postman, click en **Environments** (panel izquierdo)
2. Click en **Import**
3. Arrastra el archivo `TPI-Backend.postman_environment.json`
4. Click en **Import**
5. Selecciona el environment "TPI Backend - Local" en el dropdown superior derecho

## 🔐 Configuración de Keycloak (PREREQUISITO)

Antes de usar la colección, debes configurar Keycloak:

### 1. Crear Realm

```
Nombre: tpi-backend
```

### 2. Crear Client

```
Client ID: tpi-client
Client Protocol: openid-connect
Access Type: public
Direct Access Grants Enabled: ON
Standard Flow Enabled: OFF
Valid Redirect URIs: * (o dejar vacío)
Web Origins: *
```

### 3. Crear Roles de Realm

```
- CLIENTE
- ADMIN
- TRANSPORTISTA
```

### 4. Configurar Mapper de Roles

En **Client Scopes** → **roles** → **Mappers**, verificar que existe el mapper:

```
Name: realm roles
Mapper Type: User Realm Role
Token Claim Name: roles
Claim JSON Type: String
Add to ID token: ON
Add to access token: ON
Add to userinfo: ON
```

### 5. Crear Usuarios de Prueba

**Usuario CLIENTE:**
```
Username: cliente1
Email: cliente1@example.com
Password: password123
Email Verified: ON
Role: CLIENTE
```

**Usuario ADMIN:**
```
Username: admin1
Email: admin1@example.com
Password: password123
Email Verified: ON
Role: ADMIN
```

**Usuario TRANSPORTISTA:**
```
Username: transportista1
Email: transportista1@example.com
Password: password123
Email Verified: ON
Role: TRANSPORTISTA
```

## 📝 Uso de la Colección

### Flujo Típico de Testing

#### 1. Obtener Token

Ejecuta cualquiera de estos requests en la carpeta **Authentication**:
- `Get Token (CLIENTE)` - Para probar como cliente
- `Get Token (ADMIN)` - Para probar como administrador
- `Get Token (TRANSPORTISTA)` - Para probar como transportista

El token se guarda automáticamente en la variable `{{access_token}}` y se usa en todas las demás requests.

#### 2. Crear Solicitud (RF 1.1 y RF 1.2)

Request: **Solicitudes** → `Crear Solicitud (CU-01)`

Este endpoint implementa la **creación atómica**:
- Si el cliente no existe, lo crea automáticamente
- Si el contenedor no existe, lo crea automáticamente
- Valida que el email del JWT coincida con el email del cliente

**Body de ejemplo:**
```json
{
  "cliente": {
    "nombre": "Juan Pérez",
    "email": "cliente1@example.com",
    "telefono": "+5491112345678",
    "direccion": "Av. Corrientes 1234"
  },
  "contenedor": {
    "numeroSerie": "CONT-001",
    "tipo": "40HC",
    "peso": 1500.0,
    "volumen": 67.5
  },
  "origenDireccion": "Buenos Aires",
  "origenLatitud": -34.603722,
  "origenLongitud": -58.381592,
  "destinoDireccion": "Rosario",
  "destinoLatitud": -32.944766,
  "destinoLongitud": -60.650243
}
```

El `solicitud_id` se guarda automáticamente para usarse en otros requests.

#### 3. Consultar Solicitud

Request: **Solicitudes** → `Obtener Solicitud por ID (CU-02)`

Usa la variable `{{solicitud_id}}` que se guardó al crear la solicitud.

#### 4. Registrar Inicio/Fin de Tramo (Como TRANSPORTISTA)

1. Primero obtén un token de TRANSPORTISTA: `Get Token (TRANSPORTISTA)`
2. Ejecuta `Registrar Inicio de Tramo (CU-07)`
3. Ejecuta `Registrar Fin de Tramo (CU-08)`

## 📂 Estructura de la Colección

```
TPI Backend
├── Authentication
│   ├── Get Token (CLIENTE)
│   ├── Get Token (ADMIN)
│   ├── Get Token (TRANSPORTISTA)
│   └── Refresh Token
├── Solicitudes
│   ├── Crear Solicitud (CU-01)
│   ├── Obtener Solicitud por ID (CU-02)
│   ├── Listar Todas las Solicitudes (ADMIN)
│   ├── Solicitudes por Cliente
│   ├── Solicitudes Pendientes (CU-05)
│   ├── Solicitudes por Estado
│   ├── Asignar Ruta a Solicitud (CU-04)
│   ├── Actualizar Estado Solicitud
│   └── Actualizar Estimaciones
├── Tramos
│   ├── Registrar Inicio de Tramo (CU-07)
│   └── Registrar Fin de Tramo (CU-08)
├── Clientes
│   ├── Crear Cliente
│   ├── Obtener Cliente por ID
│   ├── Listar Todos los Clientes
│   └── Buscar Cliente por Email
├── Contenedores
│   ├── Crear Contenedor
│   ├── Obtener Contenedor por ID
│   ├── Buscar Contenedor por Número de Serie
│   ├── Contenedores por Cliente
│   └── Contenedores por Estado
├── Flota
│   ├── Listar Camiones
│   ├── Obtener Camión por ID
│   ├── Listar Depósitos
│   └── Obtener Depósito por ID
├── Costos
│   ├── Listar Tarifas
│   └── Obtener Tarifa por ID
└── Health & Actuator
    ├── Health Check
    └── Info
```

## 🔄 Variables de Entorno

La colección usa estas variables (ya configuradas en el environment):

### URLs
- `gateway_url`: http://localhost:8080
- `keycloak_url`: http://localhost:9090

### Autenticación
- `realm`: tpi-backend
- `client_id`: tpi-client
- `access_token`: (se llena automáticamente)
- `refresh_token`: (se llena automáticamente)

### Usuarios
- `cliente_username`: cliente1@example.com
- `cliente_password`: password123
- `admin_username`: admin1@example.com
- `admin_password`: password123
- `transportista_username`: transportista1@example.com
- `transportista_password`: password123

### IDs de Entidades (se llenan automáticamente)
- `solicitud_id`
- `cliente_id`
- `contenedor_id`
- `tramo_id`
- `ruta_id`
- `camion_id`
- `deposito_id`
- `tarifa_id`

## ⚡ Scripts Automatizados

### Auto-guardado de Tokens

Los requests de autenticación tienen scripts que guardan automáticamente:
- `access_token`
- `refresh_token`

### Auto-guardado de IDs

Los requests de creación guardan automáticamente los IDs:
- Crear Solicitud → guarda `solicitud_id`
- Crear Cliente → guarda `cliente_id`
- Crear Contenedor → guarda `contenedor_id`

## 🧪 Casos de Prueba Recomendados

### 1. Creación Atómica de Solicitud
1. Obtén token de CLIENTE
2. Crea solicitud con cliente y contenedor nuevos
3. Verifica que se crean automáticamente
4. Intenta crear otra solicitud con el mismo contenedor
5. Verifica que se reutiliza el contenedor existente

### 2. Control de Acceso por Roles
1. Obtén token de CLIENTE
2. Intenta listar todas las solicitudes (debería fallar - 403)
3. Obtén token de ADMIN
4. Intenta listar todas las solicitudes (debería funcionar - 200)

### 3. Validación de Email
1. Obtén token de cliente1@example.com
2. Intenta crear solicitud con email cliente2@example.com
3. Debería fallar con 403 (email no coincide)

### 4. Registro de Tramos por TRANSPORTISTA
1. Obtén token de TRANSPORTISTA
2. Registra inicio de tramo
3. Registra fin de tramo
4. Verifica que el estado cambió correctamente

## 🐛 Troubleshooting

### Error 401 Unauthorized
- Verifica que obtienes el token primero
- Verifica que el token no haya expirado (300 segundos = 5 minutos)
- Usa `Refresh Token` para obtener un nuevo token

### Error 403 Forbidden
- Verifica que tienes el rol correcto para el endpoint
- Para endpoints de ADMIN, usa `Get Token (ADMIN)`
- Para tramos, usa `Get Token (TRANSPORTISTA)`

### Error 404 Not Found
- Verifica que los servicios estén corriendo
- Verifica que usaste las variables correctas (ej: `{{solicitud_id}}`)

### Error en obtener token
- Verifica que Keycloak está corriendo en http://localhost:9090
- Verifica que el realm `tpi-backend` existe
- Verifica que el client `tpi-client` está configurado
- Verifica que los usuarios existen con las credenciales correctas

## 📞 Soporte

Si encuentras problemas:
1. Verifica que todos los servicios estén corriendo (`docker-compose up`)
2. Verifica la configuración de Keycloak
3. Revisa los logs de los servicios
4. Verifica que las variables de entorno estén correctamente configuradas
