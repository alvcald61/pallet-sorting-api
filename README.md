# Pallet Sorting API

API RESTful para gestión de órdenes de transporte y clasificación de pallets en almacenes.

## 🚀 Características

- **Gestión de Órdenes**: Creación, actualización y seguimiento de órdenes de transporte
- **Clasificación de Pallets**: Algoritmos de empaquetado 2D, 3D y Bulk
- **Gestión de Flotas**: Control de camiones y conductores
- **Tracking en Tiempo Real**: Estados de transporte con actualizaciones GPS
- **Sistema de Precios**: Cálculo automático basado en zonas y condiciones
- **Multi-almacén**: Soporte para múltiples almacenes y zonas de entrega
- **Dashboard Analítico**: Estadísticas y métricas de rendimiento
- **Autenticación JWT**: Sistema seguro de autenticación y autorización

## 🏗️ Arquitectura

Proyecto basado en **Arquitectura Hexagonal** (Ports & Adapters):

```
src/main/java/com/tupack/palletsortingapi/
├── common/              # Código compartido
│   ├── config/          # Configuraciones
│   ├── dto/             # DTOs comunes
│   ├── exception/       # Excepciones globales
│   └── service/         # Servicios base
├── order/               # Dominio de Órdenes
│   ├── domain/          # Entidades y lógica de negocio
│   ├── application/     # Casos de uso y DTOs
│   └── infrastructure/  # Adaptadores (REST, BD, Storage)
└── user/                # Dominio de Usuarios
    ├── domain/
    ├── application/
    └── infrastructure/
```

## 🛠️ Stack Tecnológico

- **Framework**: Spring Boot 3.x
- **Java**: 21
- **Base de Datos**: MySQL 8.x
- **ORM**: Hibernate/JPA
- **Seguridad**: Spring Security + JWT
- **Documentación**: OpenAPI/Swagger
- **Build**: Maven
- **Logging**: SLF4J + Logback

## 📋 Prerequisitos

- Java 21 o superior
- Maven 3.8+
- MySQL 8.0+
- (Opcional) Docker para desarrollo

## ⚙️ Configuración

### 1. Variables de Entorno

Copiar `.env.example` a `.env` y configurar:

```bash
cp .env.example .env
```

Editar las variables según tu entorno:

```env
# Base de datos
DB_URL=jdbc:mysql://localhost:3306/tupack
DB_USERNAME=root
DB_PASSWORD=tu_password

# JWT
JWT_SECRET=clave-secreta-minimo-32-caracteres
JWT_EXPIRATION=30
REFRESH_TOKEN_DAYS=7

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Storage
STORAGE_PATH=./uploads
```

### 2. Base de Datos

Crear la base de datos:

```sql
CREATE DATABASE tupack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Las tablas se crearán automáticamente con Hibernate en modo `development`.

### 3. Compilación

```bash
mvn clean install
```

### 4. Ejecución

**Modo Desarrollo:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Modo Producción:**
```bash
java -jar target/pallet-sorting-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 📚 API Documentation

Una vez iniciada la aplicación, acceder a:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## 🔐 Autenticación

La API usa JWT para autenticación.

### Registro de Usuario

```bash
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "password": "password123"
}
```

### Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "password123"
}
```

Respuesta incluye `accessToken` y `refreshToken`.

### Uso del Token

```bash
GET /api/orders
Authorization: Bearer {accessToken}
```

## 📊 Endpoints Principales

### Órdenes
- `POST /api/orders` - Crear orden con empaquetado automático
- `GET /api/orders` - Listar órdenes (paginado)
- `GET /api/orders/{id}` - Obtener orden por ID
- `PUT /api/orders/{id}/status` - Actualizar estado

### Transporte
- `POST /api/transport-status` - Actualizar estado de transporte
- `GET /api/transport-status/{orderId}` - Historial de transporte

### Camiones
- `GET /api/trucks` - Listar camiones
- `POST /api/trucks` - Crear camión
- `PUT /api/trucks/{id}` - Actualizar camión

### Dashboard
- `GET /api/dashboard/stats` - Estadísticas generales
- `GET /api/dashboard/orders-by-status` - Órdenes por estado
- `GET /api/dashboard/performance` - Métricas de rendimiento

Ver documentación completa en Swagger.

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar con cobertura
mvn test jacoco:report
```

## 📦 Estructura de Datos

### Orden
```json
{
  "clientId": "uuid",
  "fromAddress": "Almacén Central, Lima",
  "toAddress": "Av. Principal 123, Lima",
  "pickupDate": "2025-02-10T08:00:00",
  "pallets": [
    {
      "type": "STANDARD",
      "width": 120,
      "length": 100,
      "height": 150,
      "amount": 5
    }
  ]
}
```

### Estados de Orden
- `REVIEW` - En revisión
- `PRE_APPROVED` - Pre-aprobado
- `APPROVED` - Aprobado
- `IN_PROGRESS` - En camino
- `DELIVERED` - Entregado
- `DENIED` - Denegado

### Estados de Transporte
- `PENDING` - Pendiente
- `TRUCK_ASSIGNED` - Camión asignado
- `EN_ROUTE_TO_WAREHOUSE` - Camino al almacén
- `LOADING` - Cargando
- `EN_ROUTE_TO_DESTINATION` - Camino al destino
- `DELIVERED` - Entregado

## 🔧 Configuración Avanzada

### Profiles

- **dev**: Desarrollo (auto DDL, SQL visible, CORS permisivo)
- **prod**: Producción (seguridad completa, sin auto DDL)

### Logging

Configurar niveles en `application-{profile}.yml`:

```yaml
logging:
  level:
    root: INFO
    com.tupack.palletsortingapi: DEBUG
```

Logs se guardan en `logs/pallet-sorting-api-{profile}.log`

### CORS

Configurar orígenes permitidos en variables de entorno:

```env
CORS_ALLOWED_ORIGINS=https://app.tupack.com,https://admin.tupack.com
```

## 📝 Changelog

Ver [CHANGELOG.md](CHANGELOG.md) para historial de cambios.

## 🤝 Contribución

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push al branch (`git push origin feature/nueva-funcionalidad`)
5. Abrir Pull Request

## 📄 Licencia

Proyecto privado - TUPACK © 2025

## 👥 Equipo

Desarrollado por el equipo de TUPACK

## 📞 Soporte

Para soporte técnico, contactar a: soporte@tupack.com

---

**Documentación Adicional:**
- [Guía de Integración Frontend](FRONTEND_INTEGRATION_GUIDE.md)
- [Arquitectura de Servicios](SERVICE_ARCHITECTURE.md)
- [Guía de Estados de Transporte](TRANSPORT_STATUS_GUIDE.md)
- [Referencia Rápida de API](QUICK_REFERENCE.md)
