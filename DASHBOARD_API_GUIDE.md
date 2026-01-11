# Dashboard API - Guía de Uso

## Descripción General

Se ha implementado completamente la API de Dashboard con 7 endpoints que proporcionan estadísticas y análisis de órdenes, clientes, conductores y camiones.

## Estructura del Proyecto

### Ubicación de Archivos

#### DTOs (Data Transfer Objects)
```
src/main/java/com/tupack/palletsortingapi/order/infrastructure/outbound/dto/
├── DashboardStatsDTO.java
├── PendingOrderDTO.java
├── OrdersByClientDTO.java
├── OrdersByDriverDTO.java
├── OrdersByTruckDTO.java
├── OrdersByStatusDTO.java
└── PerformanceMetricsDTO.java
```

#### Servicio
```
src/main/java/com/tupack/palletsortingapi/order/application/
└── DashboardService.java
```

#### Controlador
```
src/main/java/com/tupack/palletsortingapi/order/infrastructure/inbound/controller/
└── DashboardController.java
```

## Endpoints Disponibles

### 1. GET `/api/dashboard/stats`
**Descripción**: Obtiene estadísticas generales del dashboard

**Ejemplo de Respuesta**:
```json
{
  "totalOrders": 150,
  "pendingOrders": 25,
  "deliveredOrders": 120,
  "totalRevenue": 15000.50
}
```

---

### 2. GET `/api/dashboard/pending-orders?limit=10`
**Descripción**: Obtiene las órdenes pendientes con un límite opcional

**Parámetros**:
- `limit` (query, opcional): Número máximo de órdenes. Default: 10

**Ejemplo de Respuesta**:
```json
[
  {
    "id": "1",
    "clientName": "Juan Pérez",
    "fromAddress": "Lima, Perú",
    "toAddress": "Arequipa, Perú",
    "pickupDate": "2026-01-15",
    "orderStatus": "PENDING"
  }
]
```

**Uso**:
```bash
# Obtener 10 órdenes (default)
GET http://localhost:8080/api/dashboard/pending-orders

# Obtener 20 órdenes
GET http://localhost:8080/api/dashboard/pending-orders?limit=20
```

---

### 3. GET `/api/dashboard/orders-by-client`
**Descripción**: Obtiene el conteo de órdenes agrupadas por cliente

**Ejemplo de Respuesta**:
```json
[
  {
    "id": "1",
    "clientName": "Empresa A",
    "businessName": "Empresa A S.A.",
    "count": 25
  },
  {
    "id": "2",
    "clientName": "Empresa B",
    "businessName": "Empresa B S.A.",
    "count": 18
  }
]
```

---

### 4. GET `/api/dashboard/orders-by-driver`
**Descripción**: Obtiene el conteo de órdenes agrupadas por chofer

**Ejemplo de Respuesta**:
```json
[
  {
    "id": "1",
    "driverName": "Juan Pérez",
    "name": "Juan Pérez",
    "count": 45
  },
  {
    "id": "2",
    "driverName": "Carlos García",
    "name": "Carlos García",
    "count": 32
  }
]
```

---

### 5. GET `/api/dashboard/orders-by-truck`
**Descripción**: Obtiene el conteo de órdenes agrupadas por camión

**Ejemplo de Respuesta**:
```json
[
  {
    "id": "1",
    "truckPlate": "ABC-1234",
    "plate": "ABC-1234",
    "count": 28
  },
  {
    "id": "2",
    "truckPlate": "DEF-5678",
    "plate": "DEF-5678",
    "count": 22
  }
]
```

---

### 6. GET `/api/dashboard/orders-by-status`
**Descripción**: Obtiene el conteo de órdenes agrupadas por estado

**Ejemplo de Respuesta**:
```json
[
  {
    "status": "PENDING",
    "orderStatus": "PENDING",
    "count": 25,
    "total": 25
  },
  {
    "status": "IN_TRANSIT",
    "orderStatus": "IN_TRANSIT",
    "count": 50,
    "total": 50
  },
  {
    "status": "DELIVERED",
    "orderStatus": "DELIVERED",
    "count": 75,
    "total": 75
  }
]
```

---

### 7. GET `/api/dashboard/performance-metrics`
**Descripción**: Obtiene métricas de rendimiento

**Ejemplo de Respuesta**:
```json
{
  "totalVolume": 5000.00,
  "totalWeight": 15000.50,
  "averageDeliveryTime": 2.5,
  "totalIncome": 150000.00,
  "totalOrders": 150
}
```

---

## Lógica de Implementación

### DashboardService

El servicio contiene 7 métodos públicos, uno para cada endpoint:

1. **getStats()**: Calcula estadísticas generales filtrando órdenes por estado
2. **getPendingOrders(limit)**: Filtra órdenes pendientes y las limita
3. **getOrdersByClient()**: Agrupa órdenes por cliente contando totales
4. **getOrdersByDriver()**: Agrupa órdenes por conductor (a través de TruckOrder)
5. **getOrdersByTruck()**: Agrupa órdenes por camión (a través de TruckOrder)
6. **getOrdersByStatus()**: Agrupa órdenes por estado
7. **getPerformanceMetrics()**: Calcula métricas agregadas de todas las órdenes

### Relaciones entre Entidades

```
Order
├── client (Client)
│   └── user (User)
├── truckOrder (TruckOrder)
│   └── truck (Truck)
│       └── driver (Driver)
│           └── user (User)
└── orderStatus (Enum)
```

## Características Especiales

### Manejo de Valores Nulos
Todos los métodos manejan seguramente valores nulos:
- Se valida la existencia de relaciones antes de acceder a sus atributos
- Se proporciona cadena vacía como fallback para nombres
- Se convierte BigDecimal a Double de forma segura

### Nombres Completos
- Los nombres de clientes y conductores se construyen concatenando `firstName` y `lastName` del usuario asociado
- Esto se hace en el mapa para asegurar que siempre se tiene el nombre completo

### Placa del Camión
- Se obtiene del atributo `licensePlate` de la entidad Truck
- Se duplica en dos campos (`truckPlate` y `plate`) para compatibilidad con el frontend

## Notas Técnicas

- **Spring Data JPA**: Se utilizan repositorios para acceder a la base de datos
- **Streams API**: Se usan streams de Java para filtrado y agrupamiento eficiente
- **Lombok**: Se usa para reducir código boilerplate en DTOs
- **Collectors**: Se utiliza `groupingBy` y `counting` para agregación de datos

## Posibles Mejoras Futuras

1. **Paginación**: Agregar paginación a los endpoints que retornan listas
2. **Filtros Adicionales**: Agregar filtros por fecha, rango de ingresos, etc
3. **Cálculo de Promedio**: Implementar lógica real para `averageDeliveryTime`
4. **Caché**: Agregar caching de resultados para mejorar rendimiento
5. **Proyecciones**: Usar proyecciones de Spring Data JPA para optimizar queries
6. **Query Personalizada**: Implementar queries personalizadas en lugar de cargar todo en memoria

## Testing

Para probar los endpoints, puedes usar herramientas como:

- **curl**:
  ```bash
  curl http://localhost:8080/api/dashboard/stats
  curl "http://localhost:8080/api/dashboard/pending-orders?limit=5"
  ```

- **Postman**: Importar la colección de endpoints
- **REST Client** (VS Code): Crear archivos `.http`

## Troubleshooting

### No se retornan resultados
- Verifica que existan órdenes en la base de datos
- Comprueba que las relaciones (client, truck, driver) estén correctamente establecidas

### Error de valores nulos
- Todos los métodos validan valores nulos internamente
- Si un cliente no tiene usuario, se retorna cadena vacía en lugar de excepción

### Performance
- Para bases de datos grandes, considera implementar queries personalizadas
- Los métodos actuales cargan todos los datos en memoria con `findAll()`

