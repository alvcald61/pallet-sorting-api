# 🚚 Guía de Estados de Transporte

## 📊 Arquitectura de Estados

### Estados Separados (Implementado)

Tu sistema ahora tiene **dos niveles de estados**:

```
Order
├── OrderStatus (Estado General)
│   ├── REVIEW
│   ├── PRE_APPROVED
│   ├── APPROVED
│   ├── DOCUMENT_PENDING
│   ├── IN_PROGRESS ← La orden está activa
│   ├── DELIVERED ← Completada exitosamente
│   └── DENIED ← Cancelada/rechazada
│
└── TransportStatus (Estado de Transporte - Granular)
    ├── PENDING
    ├── TRUCK_ASSIGNED
    ├── EN_ROUTE_TO_WAREHOUSE
    ├── ARRIVED_AT_WAREHOUSE
    ├── LOADING
    ├── LOADING_COMPLETED
    ├── EN_ROUTE_TO_DESTINATION
    ├── ARRIVED_AT_DESTINATION
    ├── UNLOADING
    ├── UNLOADING_COMPLETED
    └── DELIVERED
```

## 🔄 Flujo de Estados

### Flujo General de una Orden

```
1. Cliente crea orden → REVIEW
2. Admin revisa → PRE_APPROVED (con monto)
3. Cliente aprueba → APPROVED
4. Sistema valida documentos → DOCUMENT_PENDING (si aplica)
5. Documentos subidos → IN_PROGRESS
6. Transporte completo → DELIVERED
```

### Flujo Detallado de Transporte (cuando OrderStatus = IN_PROGRESS)

```
PENDING
   ↓ (Se asigna camión)
TRUCK_ASSIGNED
   ↓ (Camión sale hacia almacén)
EN_ROUTE_TO_WAREHOUSE
   ↓ (Camión llega)
ARRIVED_AT_WAREHOUSE
   ↓ (Inicia carga)
LOADING
   ↓ (Termina carga)
LOADING_COMPLETED
   ↓ (Camión sale hacia destino)
EN_ROUTE_TO_DESTINATION
   ↓ (Camión llega a destino)
ARRIVED_AT_DESTINATION
   ↓ (Inicia descarga)
UNLOADING
   ↓ (Termina descarga)
UNLOADING_COMPLETED
   ↓ (Entrega confirmada)
DELIVERED → OrderStatus también cambia a DELIVERED
```

## 📡 API Endpoints

### 1. Actualizar Estado de Transporte (Completo)

```http
PATCH /api/order/{orderId}/transport/status
Content-Type: application/json

{
  "status": "LOADING",
  "latitude": -12.0464,
  "longitude": -77.0428,
  "address": "Av. Argentina 123, Callao",
  "notes": "Iniciando carga de 15 pallets",
  "photoUrl": "https://s3.../photo123.jpg"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Transport status updated successfully to: Cargando",
  "data": null
}
```

### 2. Actualizar Estado Rápido (Sin ubicación)

```http
PATCH /api/order/{orderId}/transport/status/quick?status=TRUCK_ASSIGNED
```

### 3. Obtener Historial de Transporte

```http
GET /api/order/{orderId}/transport/history
```

**Respuesta:**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "status": "LOADING",
      "statusDisplayName": "Cargando",
      "timestamp": "2026-02-02T15:30:00",
      "locationLatitude": -12.0464,
      "locationLongitude": -77.0428,
      "locationAddress": "Av. Argentina 123, Callao",
      "notes": "Iniciando carga de 15 pallets",
      "updatedBy": "driver@tupack.com",
      "photoUrl": "https://s3.../photo123.jpg"
    },
    {
      "id": 4,
      "status": "ARRIVED_AT_WAREHOUSE",
      "timestamp": "2026-02-02T15:00:00",
      ...
    }
  ]
}
```

### 4. Obtener Timeline con GPS

```http
GET /api/order/{orderId}/transport/timeline
```

Retorna solo las actualizaciones que tienen coordenadas GPS (útil para mapas).

### 5. Obtener Estado Actual

```http
GET /api/order/{orderId}/transport/status
```

## 🗄️ Modelo de Base de Datos

### Nueva Tabla: `transport_status_updates`

```sql
CREATE TABLE transport_status_updates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp DATETIME NOT NULL,
    location_latitude DOUBLE,
    location_longitude DOUBLE,
    location_address VARCHAR(500),
    notes VARCHAR(1000),
    updated_by VARCHAR(100),
    photo_url VARCHAR(500),
    signature_url VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (order_id) REFERENCES transport_order(id)
);
```

### Columna Agregada a `transport_order`

```sql
ALTER TABLE transport_order
ADD COLUMN transport_status VARCHAR(50);
```

## 💡 Casos de Uso

### Caso 1: Driver Actualiza Estado desde App Móvil

```java
// App móvil envía ubicación GPS cuando llega al almacén
POST /api/order/123/transport/status
{
  "status": "ARRIVED_AT_WAREHOUSE",
  "latitude": -12.0464,
  "longitude": -77.0428,
  "notes": "Llegué al almacén, esperando personal de carga"
}
```

### Caso 2: Sistema Automático al Asignar Truck

```java
// En OrderSchedulingService.assignTruckToOrder()
@Autowired
private TransportStatusService transportStatusService;

private void assignTruckToOrder(Truck truck, Order order) {
    truck.getOrders().add(order);
    truck.setStatus(TruckStatus.ASSIGNED);
    truckRepository.save(truck);

    // Inicializar tracking de transporte
    transportStatusService.initializeTransportStatus(order);
}
```

### Caso 3: Driver Toma Foto al Completar Carga

```java
// 1. Driver sube foto a S3/storage
String photoUrl = uploadPhoto(photo);

// 2. Actualiza estado con evidencia fotográfica
POST /api/order/123/transport/status
{
  "status": "LOADING_COMPLETED",
  "photoUrl": "https://s3.../loading_complete_123.jpg",
  "latitude": -12.0464,
  "longitude": -77.0428,
  "notes": "15/15 pallets cargados"
}
```

### Caso 4: Cliente ve Timeline en Tiempo Real

```javascript
// Frontend hace polling cada 30 segundos
setInterval(async () => {
  const timeline = await fetch(`/api/order/${orderId}/transport/timeline`);
  updateMapWithLocations(timeline.data);
}, 30000);
```

## 🔒 Validaciones Implementadas

### 1. Validación de Transiciones

```java
// Solo permite transiciones válidas
PENDING → TRUCK_ASSIGNED ✅
PENDING → LOADING ❌ (Inválido)
```

### 2. Validación de Estado de Orden

```java
// Solo permite updates de transporte si OrderStatus = IN_PROGRESS
if (order.getOrderStatus() != OrderStatus.IN_PROGRESS) {
    throw new BusinessException("Order must be IN_PROGRESS");
}
```

### 3. Auto-completado de Orden

```java
// Cuando TransportStatus = DELIVERED, cambia OrderStatus a DELIVERED
if (newStatus == TransportStatus.DELIVERED) {
    order.setOrderStatus(OrderStatus.DELIVERED);
    order.setRealDeliveryDate(LocalDateTime.now());
}
```

## 📈 Ventajas del Sistema

### 1. Separación de Responsabilidades
- **OrderStatus**: Gestión administrativa y aprobaciones
- **TransportStatus**: Operaciones logísticas y tracking

### 2. Historial Completo
- Cada cambio de estado queda registrado
- Incluye: timestamp, ubicación GPS, notas, fotos, usuario

### 3. Trazabilidad GPS
- Tracking en tiempo real de la ubicación del camión
- Útil para:
  - Mostrar mapa en frontend
  - Calcular ETAs
  - Auditorías

### 4. Evidencia Fotográfica
- Fotos de carga/descarga
- Firmas digitales
- Comprobantes de entrega

### 5. Queries Eficientes
```java
// Solo órdenes en tránsito
orders.where(transportStatus.isInTransit())

// Órdenes retrasadas
orders.where(projectedDelivery < now && !transportStatus.isTerminal())
```

## 🎯 Próximos Pasos Recomendados

### 1. Crear Migración de Base de Datos

```sql
-- V2__add_transport_status.sql
ALTER TABLE transport_order
ADD COLUMN transport_status VARCHAR(50);

CREATE TABLE transport_status_updates (
    -- [definición completa arriba]
);
```

### 2. Actualizar Órdenes Existentes

```java
// Script de migración de datos
@Service
public class TransportStatusMigrationService {

    public void migrateExistingOrders() {
        List<Order> inProgressOrders = orderRepository
            .findByOrderStatus(OrderStatus.IN_PROGRESS);

        for (Order order : inProgressOrders) {
            // Inferir estado basado en datos existentes
            TransportStatus status = inferTransportStatus(order);
            order.setTransportStatus(status);

            // Crear registro histórico
            TransportStatusUpdate update = new TransportStatusUpdate(order, status);
            update.setUpdatedBy("MIGRATION");
            transportStatusUpdateRepository.save(update);
        }

        orderRepository.saveAll(inProgressOrders);
    }
}
```

### 3. Integrar con Frontend

```typescript
// React/Vue component
interface TransportUpdate {
  status: TransportStatus;
  timestamp: string;
  location?: {
    lat: number;
    lng: number;
    address: string;
  };
  notes?: string;
  photoUrl?: string;
}

function OrderTrackingMap({ orderId }: { orderId: number }) {
  const [timeline, setTimeline] = useState<TransportUpdate[]>([]);

  useEffect(() => {
    // Fetch initial data
    fetch(`/api/order/${orderId}/transport/timeline`)
      .then(r => r.json())
      .then(data => setTimeline(data.data));

    // Poll for updates
    const interval = setInterval(() => {
      fetch(`/api/order/${orderId}/transport/timeline`)
        .then(r => r.json())
        .then(data => setTimeline(data.data));
    }, 30000); // 30 seconds

    return () => clearInterval(interval);
  }, [orderId]);

  return <GoogleMap markers={timeline.map(t => t.location)} />;
}
```

### 4. Notificaciones Push

```java
@Service
public class TransportNotificationService {

    @EventListener
    public void onTransportStatusUpdate(TransportStatusUpdateEvent event) {
        Order order = event.getOrder();
        TransportStatus newStatus = event.getNewStatus();

        // Notificar al cliente
        notificationService.sendPush(
            order.getClient().getUser().getId(),
            "Tu pedido está " + newStatus.getDisplayName(),
            event.getLocation()
        );

        // Si es crítico, notificar al admin
        if (newStatus.isActiveOperation()) {
            notificationService.notifyAdmins(
                "Operación en curso: " + order.getId()
            );
        }
    }
}
```

### 5. Dashboard de Monitoreo

Crea queries útiles:

```java
// Órdenes actualmente en tránsito
List<Order> inTransit = orderRepository.findByTransportStatusIn(
    List.of(
        TransportStatus.EN_ROUTE_TO_WAREHOUSE,
        TransportStatus.EN_ROUTE_TO_DESTINATION
    )
);

// Órdenes en operaciones activas (carga/descarga)
List<Order> activeOps = orderRepository.findByTransportStatusIn(
    List.of(TransportStatus.LOADING, TransportStatus.UNLOADING)
);

// Promedio de tiempo por estado
@Query("SELECT t.status, AVG(TIMESTAMPDIFF(MINUTE, t.timestamp, t2.timestamp)) "
     + "FROM TransportStatusUpdate t "
     + "JOIN TransportStatusUpdate t2 ON t.order.id = t2.order.id "
     + "WHERE t2.timestamp > t.timestamp "
     + "GROUP BY t.status")
Map<TransportStatus, Double> getAverageTimePerStatus();
```

## ⚠️ Consideraciones Importantes

1. **Permisos**: Solo drivers y admins deben poder actualizar estados de transporte
2. **Validación de GPS**: Validar que las coordenadas sean razonables
3. **Almacenamiento de Fotos**: Usar S3/Cloud Storage, no guardar en BD
4. **Rate Limiting**: Limitar updates a 1 por minuto para evitar spam
5. **Transacciones**: Todas las actualizaciones deben ser transaccionales

## 📚 Recursos Adicionales

- Ver `TransportStatus.java` para todas las transiciones válidas
- Ver `TransportStatusService.java` para la lógica de negocio
- Ver `TransportStatusController.java` para ejemplos de uso de API
