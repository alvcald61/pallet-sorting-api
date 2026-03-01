# 📡 Nuevos Endpoints Requeridos para Mejoras de UX

## 1. Cálculo de Costo Estimado

### `POST /api/order/estimate-cost`

**Descripción:** Calcula el costo estimado de un pedido basándose en volumen, peso y distancia.

**Request Body:**
```json
{
  "orderType": "BULK" | "TWO_DIMENSIONAL",
  "items": [
    {
      "volume": 20,
      "weight": 500,
      "quantity": 2
    }
  ],
  "fromAddress": {
    "address": "Av. Principal 123",
    "district": "Lima",
    "city": "Lima",
    "state": "Lima"
  },
  "toAddress": {
    "address": "Calle Secundaria 456",
    "district": "Callao",
    "city": "Callao",
    "state": "Callao"
  },
  "pickupDate": "2026-02-15"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "estimatedCost": 1250.50,
    "breakdown": {
      "baseCost": 1000.00,
      "volumeCost": 800.00,
      "weightCost": 200.00,
      "distanceCost": 250.50,
      "urgencyFee": 0
    },
    "distance": {
      "kilometers": 45.2,
      "estimatedDuration": "1h 15m"
    },
    "currency": "USD"
  }
}
```

**Lógica de Cálculo Sugerida:**
1. Costo base por volumen: `volume (m³) * $50`
2. Costo por peso: `(weight kg / 100) * $10`
3. Multiplicador de distancia: Usar Google Maps Distance Matrix API
4. Tarifa de urgencia: Si pickupDate < 48h agregar 20%

---

## 2. Validación de Dirección

### `POST /api/address/validate`

**Descripción:** Valida y normaliza direcciones usando Google Maps Geocoding API.

**Request Body:**
```json
{
  "address": "Av. Javier Prado 123",
  "district": "San Isidro",
  "city": "Lima",
  "state": "Lima"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "isValid": true,
    "normalized": {
      "address": "Av. Javier Prado Este 123",
      "district": "San Isidro",
      "city": "Lima",
      "state": "Lima",
      "country": "Perú"
    },
    "coordinates": {
      "latitude": -12.0965,
      "longitude": -77.0428
    },
    "placeId": "ChIJXxxxx",
    "confidence": "HIGH" | "MEDIUM" | "LOW"
  }
}
```

---

## 3. Sugerencias de Pedidos Anteriores

### `GET /api/order/suggestions`

**Descripción:** Obtiene sugerencias basadas en pedidos anteriores del cliente.

**Query Parameters:**
- `userId`: ID del usuario
- `limit`: Número máximo de sugerencias (default: 5)

**Response:**
```json
{
  "success": true,
  "data": {
    "frequentItems": [
      {
        "type": "BULK",
        "volume": 20,
        "weight": 500,
        "quantity": 2,
        "frequency": 15,
        "lastUsed": "2026-01-20"
      }
    ],
    "frequentRoutes": [
      {
        "fromAddress": "Av. Principal 123, Lima",
        "toAddress": "Calle Secundaria 456, Callao",
        "frequency": 8,
        "lastUsed": "2026-02-01"
      }
    ],
    "templates": [
      {
        "id": "template-1",
        "name": "Envío semanal a Callao",
        "items": [...],
        "route": {...}
      }
    ]
  }
}
```

---

## 4. Guardar Plantilla de Pedido

### `POST /api/order/template`

**Descripción:** Guarda un pedido como plantilla reutilizable.

**Request Body:**
```json
{
  "name": "Envío semanal a Callao",
  "description": "Pedido recurrente de pallets",
  "orderType": "TWO_DIMENSIONAL",
  "items": [
    {
      "length": 1.2,
      "width": 0.8,
      "height": 1.5,
      "weight": 500,
      "quantity": 4
    }
  ],
  "defaultRoute": {
    "fromWarehouseId": 1,
    "toAddress": {
      "address": "Calle Secundaria 456",
      "district": "Callao",
      "city": "Callao",
      "state": "Callao"
    }
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "templateId": "template-xyz",
    "message": "Plantilla creada exitosamente"
  }
}
```

---

## 5. Obtener Plantillas del Usuario

### `GET /api/order/templates`

**Query Parameters:**
- `userId`: ID del usuario

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "template-1",
      "name": "Envío semanal a Callao",
      "description": "Pedido recurrente de pallets",
      "orderType": "TWO_DIMENSIONAL",
      "items": [...],
      "defaultRoute": {...},
      "createdAt": "2026-01-15",
      "lastUsed": "2026-02-05"
    }
  ]
}
```

---

## 6. Calcular Distancia entre Direcciones

### `POST /api/distance/calculate`

**Descripción:** Calcula la distancia y tiempo estimado entre dos direcciones.

**Request Body:**
```json
{
  "origin": {
    "address": "Av. Principal 123, Lima"
  },
  "destination": {
    "address": "Calle Secundaria 456, Callao"
  },
  "mode": "DRIVING" | "WALKING" | "BICYCLING"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "distance": {
      "value": 45200,
      "text": "45.2 km"
    },
    "duration": {
      "value": 4500,
      "text": "1h 15m"
    },
    "route": "via Av. Venezuela"
  }
}
```

**Integración:**
- Usar Google Maps Distance Matrix API
- Almacenar cache de rutas frecuentes

---

## 7. Disponibilidad de Camiones

### `GET /api/trucks/availability`

**Descripción:** Verifica disponibilidad de camiones para una fecha específica.

**Query Parameters:**
- `date`: Fecha de recojo (YYYY-MM-DD)
- `requiredVolume`: Volumen total requerido (m³)
- `requiredWeight`: Peso total requerido (kg)

**Response:**
```json
{
  "success": true,
  "data": {
    "available": true,
    "trucks": [
      {
        "truckId": "truck-1",
        "licensePlate": "ABC-123",
        "capacity": {
          "volume": 50,
          "weight": 5000
        },
        "availableSlots": ["08:00", "10:00", "14:00"]
      }
    ],
    "recommendations": {
      "preferredTimeSlot": "08:00",
      "reason": "Menos tráfico, ruta más rápida"
    }
  }
}
```

---

## 8. Análisis de Pedido

### `POST /api/order/analyze`

**Descripción:** Analiza un pedido y proporciona optimizaciones sugeridas.

**Request Body:**
```json
{
  "items": [...],
  "route": {...},
  "pickupDate": "2026-02-15"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "analysis": {
      "totalVolume": 40.5,
      "totalWeight": 1200,
      "truckUtilization": 0.81,
      "optimizationScore": 85
    },
    "suggestions": [
      {
        "type": "CONSOLIDATION",
        "message": "Podrías agregar 10m³ más sin costo adicional",
        "potentialSavings": 150
      },
      {
        "type": "TIMING",
        "message": "Cambiar a fecha de recojo +2 días reduce costo en 10%",
        "alternativeDate": "2026-02-17",
        "potentialSavings": 125
      }
    ],
    "warnings": [
      {
        "type": "PEAK_HOUR",
        "message": "Hora de recojo coincide con tráfico pesado",
        "impact": "Posible retraso de 30-45 minutos"
      }
    ]
  }
}
```

---

## Prioridad de Implementación

### Alta Prioridad (Semana 1)
1. ✅ Cálculo de Costo Estimado
2. ✅ Validación de Dirección
3. ✅ Calcular Distancia

### Media Prioridad (Semana 2)
4. Sugerencias de Pedidos Anteriores
5. Disponibilidad de Camiones

### Baja Prioridad (Semana 3)
6. Guardar/Obtener Plantillas
7. Análisis de Pedido

---

## Notas Técnicas

### Integración con Google Maps
- Requiere Google Maps JavaScript API
- Google Distance Matrix API
- Google Geocoding API
- Configurar billing y limits en Google Cloud Console

### Caching
- Cachear rutas frecuentes por 24h
- Cachear disponibilidad de camiones por 1h
- Cachear estimaciones de costo por 30 min

### Rate Limiting
- Máximo 100 requests/minuto por endpoint
- Máximo 10 cálculos de costo por minuto por usuario

### Seguridad
- Validar que usuario solo acceda a sus propios datos
- Rate limiting para prevenir abuse
- Validar inputs para prevenir injection
