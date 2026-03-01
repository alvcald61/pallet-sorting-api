# Implementación de Filtros para getAllOrders

## Resumen
Se ha implementado un sistema de filtros dinámicos para el endpoint `GET /api/order` utilizando JPA Specifications, permitiendo filtrar órdenes por múltiples criterios.

## Archivos Modificados

### 1. **OrderSpecification.java** (NUEVO)
- **Ubicación:** `src/main/java/com/tupack/palletsortingapi/order/infrastructure/outbound/database/`
- **Función:** Clase que construye especificaciones dinámicas para filtrar órdenes
- **Filtros soportados:**
  - `search`: Búsqueda en nombre del cliente, direcciones, o placa del camión
  - `statuses`: Lista de estados de orden (REVIEW, IN_PROGRESS, DELIVERED, etc.)
  - `orderType`: Tipo de empaque (BULK, TWO_DIMENSIONAL, THREE_DIMENSIONAL)
  - `pickupDateFrom`: Fecha de inicio del rango de recogida (formato: yyyy-MM-dd)
  - `pickupDateTo`: Fecha fin del rango de recogida (formato: yyyy-MM-dd)
  - `clientId`: ID del cliente (aplicado automáticamente para rol CLIENT)
  - `driverId`: ID del conductor (aplicado automáticamente para rol DRIVER)

### 2. **OrderRepository.java** (MODIFICADO)
- **Cambio:** Extendido para incluir `JpaSpecificationExecutor<Order>`
- **Antes:** `public interface OrderRepository extends JpaRepository<Order, Long>`
- **Después:** `public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>`
- **Función:** Permite ejecutar consultas con Specifications

### 3. **OrderQueryService.java** (MODIFICADO)
- **Método actualizado:** `getAllOrders()`
- **Cambios:**
  - Ahora utiliza los parámetros de filtro (search, statuses, orderType, pickupDateFrom, pickupDateTo)
  - Construye una Specification usando `OrderSpecification.buildSpecification()`
  - Aplica filtros según el rol del usuario:
    - **ADMIN**: Puede ver todas las órdenes con filtros opcionales
    - **CLIENT**: Solo ve sus propias órdenes + filtros opcionales
    - **DRIVER**: Solo ve órdenes asignadas a él + filtros opcionales

### 4. **OrderController.java** (YA EXISTÍA - SIN CAMBIOS)
- El controlador ya tenía definidos los parámetros de filtro
- Pasa todos los parámetros al OrderService

### 5. **OrderService.java** (YA EXISTÍA - SIN CAMBIOS)
- Ya reenvía todos los parámetros al OrderQueryService

## Uso del Endpoint

### Ejemplo sin filtros
```http
GET /api/order?page=0&size=10
```

### Ejemplo con búsqueda
```http
GET /api/order?search=juan&page=0&size=10
```
Busca órdenes donde el cliente se llame "juan", o la dirección contenga "juan", o la placa del camión contenga "juan".

### Ejemplo con múltiples estados
```http
GET /api/order?statuses=REVIEW,IN_PROGRESS&page=0&size=10
```
Filtra órdenes con estado REVIEW o IN_PROGRESS.

### Ejemplo con tipo de orden
```http
GET /api/order?orderType=BULK&page=0&size=10
```
Filtra solo órdenes de tipo BULK.

### Ejemplo con rango de fechas
```http
GET /api/order?pickupDateFrom=2024-01-01&pickupDateTo=2024-12-31&page=0&size=10
```
Filtra órdenes con fecha de recogida entre enero y diciembre de 2024.

### Ejemplo combinando múltiples filtros
```http
GET /api/order?search=lima&statuses=IN_PROGRESS,DELIVERED&orderType=TWO_DIMENSIONAL&pickupDateFrom=2024-06-01&pickupDateTo=2024-12-31&page=0&size=20
```

## Comportamiento por Rol

### ADMIN
- Puede ver todas las órdenes del sistema
- Aplica solo los filtros especificados en los parámetros

### CLIENT
- Solo ve sus propias órdenes
- El filtro `clientId` se aplica automáticamente
- Puede aplicar filtros adicionales sobre sus órdenes

### DRIVER
- Solo ve órdenes asignadas a él (a través del camión)
- El filtro `driverId` se aplica automáticamente
- Puede aplicar filtros adicionales sobre sus órdenes

## Campos de Búsqueda (search)

El parámetro `search` busca en los siguientes campos:
- `client.user.firstName` - Nombre del cliente
- `client.user.lastName` - Apellido del cliente
- `client.businessName` - Nombre del negocio
- `fromAddress` - Dirección de origen
- `toAddress` - Dirección de destino
- `truck.licensePlate` - Placa del camión

La búsqueda es insensible a mayúsculas/minúsculas y busca coincidencias parciales.

## Estados Válidos (statuses)

Los valores válidos para el filtro `statuses` son:
- `REVIEW`
- `IN_PROGRESS`
- `DELIVERED`
- `CANCELLED`
- `PENDING`

## Tipos de Orden Válidos (orderType)

Los valores válidos para el filtro `orderType` son:
- `BULK`
- `TWO_DIMENSIONAL`
- `THREE_DIMENSIONAL`

## Formato de Fechas

Las fechas deben enviarse en formato: `yyyy-MM-dd`
- Ejemplo válido: `2024-12-31`
- Ejemplo inválido: `31/12/2024` o `12-31-2024`

## Paginación

Todos los resultados están paginados. Parámetros de paginación:
- `page`: Número de página (comienza en 0)
- `size`: Número de elementos por página
- `sort`: Ordenamiento (opcional, ej: `sort=pickupDate,desc`)

Ejemplo:
```http
GET /api/order?page=0&size=20&sort=pickupDate,desc&statuses=IN_PROGRESS
```

## Respuesta

La respuesta incluye:
```json
{
  "message": "string",
  "statusCode": 200,
  "data": [
    {
      "id": 1,
      "client": { ... },
      "orderStatus": "IN_PROGRESS",
      "orderType": "BULK",
      "pickupDate": "2024-12-15T10:00:00",
      ...
    }
  ],
  "pageInfo": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 45,
    "totalPages": 5
  }
}
```

## Notas Técnicas

- Los filtros se combinan con operador **AND** (todos los filtros deben cumplirse)
- Los filtros vacíos o null son ignorados
- El filtro de búsqueda combina múltiples campos con operador **OR**
- Los filtros de rol (clientId/driverId) se aplican automáticamente según el usuario autenticado
- La consulta utiliza EntityGraph para optimizar las consultas de relaciones

## Testing

Para probar los filtros, puedes usar herramientas como:
- Postman
- cURL
- Swagger UI (disponible en `/swagger-ui.html`)

Ejemplo con cURL:
```bash
curl -X GET "http://localhost:8080/api/order?search=juan&statuses=REVIEW,IN_PROGRESS&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
