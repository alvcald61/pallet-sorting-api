# Ejemplos de Respuestas JSON - Dashboard API

## 1. GET /api/dashboard/stats

### Request
```
GET http://localhost:8080/api/dashboard/stats
```

### Response (200 OK)
```json
{
  "totalOrders": 150,
  "pendingOrders": 25,
  "deliveredOrders": 120,
  "totalRevenue": 15000.50
}
```

---

## 2. GET /api/dashboard/pending-orders (default limit=10)

### Request
```
GET http://localhost:8080/api/dashboard/pending-orders
```

### Response (200 OK)
```json
[
  {
    "id": "1",
    "clientName": "Juan Pérez",
    "fromAddress": "Av. Principal 123, Lima",
    "toAddress": "Av. Secundaria 456, Arequipa",
    "pickupDate": "2026-01-15",
    "orderStatus": "PENDING"
  },
  {
    "id": "2",
    "clientName": "María García",
    "fromAddress": "Calle 1 789, Cusco",
    "toAddress": "Calle 2 101, Puno",
    "pickupDate": "2026-01-16",
    "orderStatus": "PENDING"
  },
  {
    "id": "3",
    "clientName": "Carlos López",
    "fromAddress": "Jr. Ayacucho 234, Huancayo",
    "toAddress": "Jr. Junín 567, Junín",
    "pickupDate": "2026-01-17",
    "orderStatus": "PENDING"
  }
]
```

### Request con Parámetro Personalizado
```
GET http://localhost:8080/api/dashboard/pending-orders?limit=5
```

---

## 3. GET /api/dashboard/orders-by-client

### Request
```
GET http://localhost:8080/api/dashboard/orders-by-client
```

### Response (200 OK)
```json
[
  {
    "id": "1",
    "clientName": "Empresa Transportes S.A.",
    "businessName": "Empresa Transportes S.A.",
    "count": 35
  },
  {
    "id": "2",
    "clientName": "Logística Global Inc.",
    "businessName": "Logística Global Inc.",
    "count": 28
  },
  {
    "id": "3",
    "clientName": "Distribuidora Rápida Ltd.",
    "businessName": "Distribuidora Rápida Ltd.",
    "count": 22
  },
  {
    "id": "4",
    "clientName": "Express Delivery Corp.",
    "businessName": "Express Delivery Corp.",
    "count": 18
  },
  {
    "id": "5",
    "clientName": "Comercio Electrónico Plus",
    "businessName": "Comercio Electrónico Plus",
    "count": 47
  }
]
```

---

## 4. GET /api/dashboard/orders-by-driver

### Request
```
GET http://localhost:8080/api/dashboard/orders-by-driver
```

### Response (200 OK)
```json
[
  {
    "id": "1",
    "driverName": "Juan Carlos Mendoza",
    "name": "Juan Carlos Mendoza",
    "count": 45
  },
  {
    "id": "2",
    "driverName": "Roberto Fernández",
    "name": "Roberto Fernández",
    "count": 38
  },
  {
    "id": "3",
    "driverName": "Miguel Rodríguez",
    "name": "Miguel Rodríguez",
    "count": 32
  },
  {
    "id": "4",
    "driverName": "Pedro Sánchez",
    "name": "Pedro Sánchez",
    "count": 25
  },
  {
    "id": "5",
    "driverName": "Antonio García",
    "name": "Antonio García",
    "count": 10
  }
]
```

---

## 5. GET /api/dashboard/orders-by-truck

### Request
```
GET http://localhost:8080/api/dashboard/orders-by-truck
```

### Response (200 OK)
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
    "count": 35
  },
  {
    "id": "3",
    "truckPlate": "GHI-9012",
    "plate": "GHI-9012",
    "count": 22
  },
  {
    "id": "4",
    "truckPlate": "JKL-3456",
    "plate": "JKL-3456",
    "count": 30
  },
  {
    "id": "5",
    "truckPlate": "MNO-7890",
    "plate": "MNO-7890",
    "count": 35
  }
]
```

---

## 6. GET /api/dashboard/orders-by-status

### Request
```
GET http://localhost:8080/api/dashboard/orders-by-status
```

### Response (200 OK)
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

## 7. GET /api/dashboard/performance-metrics

### Request
```
GET http://localhost:8080/api/dashboard/performance-metrics
```

### Response (200 OK)
```json
{
  "totalVolume": 5000.50,
  "totalWeight": 15000.75,
  "averageDeliveryTime": 2.5,
  "totalIncome": 150000.00,
  "totalOrders": 150
}
```

---

## Códigos de Error

### 400 Bad Request
Ocurre cuando se envían parámetros inválidos (ej: limit con valor negativo)

```json
{
  "status": 400,
  "message": "Bad Request",
  "timestamp": "2026-01-10T11:30:00.000Z"
}
```

### 500 Internal Server Error
Ocurre si hay un error en el servidor

```json
{
  "status": 500,
  "message": "Internal Server Error",
  "timestamp": "2026-01-10T11:30:00.000Z"
}
```

---

## Notas sobre las Respuestas

### Tipos de Datos
- **id**: String (ID convertido a String)
- **count**: Long (cantidad de registros)
- **total**: Long (igual a count en este caso)
- **clientName**: String (firstName + " " + lastName del usuario)
- **driverName**: String (firstName + " " + lastName del usuario)
- **truckPlate**: String (licensePlate del camión)
- **totalRevenue**: Double (suma del campo amount)
- **totalVolume**: Double (suma de totalVolume)
- **totalWeight**: Double (suma de totalWeight)
- **averageDeliveryTime**: Double (valor calculado o placeholder)
- **totalIncome**: Double (suma del field amount)

### Valores Nulos
- Si un cliente no tiene usuario asociado, clientName será una cadena vacía
- Si un conductor no tiene usuario asociado, driverName será una cadena vacía
- Los valores numéricos siempre serán 0.0 si son nulos en la base de datos

### Ordenamiento
Por defecto, los resultados se retornan en el orden que devuelve el repositorio.
Para ordenamiento específico, se puede implementar en versiones futuras.

---

## Ejemplos con curl

```bash
# Estadísticas
curl -X GET "http://localhost:8080/api/dashboard/stats" \
  -H "Content-Type: application/json"

# Órdenes pendientes (default)
curl -X GET "http://localhost:8080/api/dashboard/pending-orders" \
  -H "Content-Type: application/json"

# Órdenes pendientes con limit
curl -X GET "http://localhost:8080/api/dashboard/pending-orders?limit=5" \
  -H "Content-Type: application/json"

# Órdenes por cliente
curl -X GET "http://localhost:8080/api/dashboard/orders-by-client" \
  -H "Content-Type: application/json"

# Órdenes por conductor
curl -X GET "http://localhost:8080/api/dashboard/orders-by-driver" \
  -H "Content-Type: application/json"

# Órdenes por camión
curl -X GET "http://localhost:8080/api/dashboard/orders-by-truck" \
  -H "Content-Type: application/json"

# Órdenes por estado
curl -X GET "http://localhost:8080/api/dashboard/orders-by-status" \
  -H "Content-Type: application/json"

# Métricas de rendimiento
curl -X GET "http://localhost:8080/api/dashboard/performance-metrics" \
  -H "Content-Type: application/json"
```

