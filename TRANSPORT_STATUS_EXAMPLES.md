# 🚀 Ejemplos de Uso - Sistema de Estados de Transporte

## 📱 Ejemplos de Integración

### 1. App Móvil del Conductor

#### Flujo Completo: Desde Asignación hasta Entrega

```kotlin
// Kotlin/Android Example
class DriverTransportActivity : AppCompatActivity() {

    private lateinit var orderId: Long
    private lateinit var locationManager: LocationManager

    // 1. Conductor acepta orden (Admin ya asignó el truck)
    fun onAcceptOrder() {
        updateTransportStatus(
            status = "TRUCK_ASSIGNED",
            notes = "Conductor aceptó la orden"
        )
    }

    // 2. Conductor inicia viaje al almacén
    fun onStartToWarehouse() {
        val location = getCurrentLocation()
        updateTransportStatus(
            status = "EN_ROUTE_TO_WAREHOUSE",
            latitude = location.latitude,
            longitude = location.longitude,
            notes = "Saliendo hacia almacén de origen"
        )

        // Iniciar tracking GPS en background
        startGPSTracking()
    }

    // 3. Conductor llega al almacén
    fun onArriveAtWarehouse() {
        val location = getCurrentLocation()
        updateTransportStatus(
            status = "ARRIVED_AT_WAREHOUSE",
            latitude = location.latitude,
            longitude = location.longitude,
            address = getAddressFromLocation(location),
            notes = "Llegué al almacén"
        )
    }

    // 4. Inicia carga (con foto)
    fun onStartLoading() {
        val photo = takePhoto()
        val photoUrl = uploadToS3(photo)

        updateTransportStatus(
            status = "LOADING",
            photoUrl = photoUrl,
            notes = "Iniciando carga de mercancía"
        )
    }

    // 5. Completa carga (con foto de confirmación)
    fun onCompleteLoading(palletCount: Int) {
        val photo = takePhoto()
        val photoUrl = uploadToS3(photo)

        updateTransportStatus(
            status = "LOADING_COMPLETED",
            photoUrl = photoUrl,
            notes = "Carga completada: $palletCount pallets"
        )
    }

    // 6. Inicia viaje a destino
    fun onStartToDestination() {
        val location = getCurrentLocation()
        updateTransportStatus(
            status = "EN_ROUTE_TO_DESTINATION",
            latitude = location.latitude,
            longitude = location.longitude,
            notes = "En camino al destino"
        )
    }

    // 7. Llega a destino
    fun onArriveAtDestination() {
        val location = getCurrentLocation()
        updateTransportStatus(
            status = "ARRIVED_AT_DESTINATION",
            latitude = location.latitude,
            longitude = location.longitude,
            address = getAddressFromLocation(location)
        )
    }

    // 8. Inicia descarga
    fun onStartUnloading() {
        updateTransportStatus(
            status = "UNLOADING",
            notes = "Iniciando descarga"
        )
    }

    // 9. Completa descarga y entrega (con firma)
    fun onCompleteDelivery(signature: Bitmap) {
        val photo = takePhoto() // Foto de descarga
        val photoUrl = uploadToS3(photo)

        val signatureUrl = uploadSignatureToS3(signature)

        api.updateTransportStatus(orderId, TransportStatusRequest(
            status = "DELIVERED",
            photoUrl = photoUrl,
            signatureUrl = signatureUrl,
            notes = "Entrega completada y firmada por cliente"
        ))
    }

    // Helper: Actualizar estado
    private fun updateTransportStatus(
        status: String,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
        notes: String? = null,
        photoUrl: String? = null
    ) {
        val request = TransportStatusRequest(
            status = status,
            latitude = latitude,
            longitude = longitude,
            address = address,
            notes = notes,
            photoUrl = photoUrl
        )

        api.updateTransportStatus(orderId, request)
            .enqueue(object : Callback<Response> {
                override fun onResponse(call: Call<Response>, response: Response) {
                    if (response.isSuccessful) {
                        showToast("Estado actualizado: $status")
                        refreshUI()
                    }
                }
                override fun onFailure(call: Call<Response>, t: Throwable) {
                    showError("Error al actualizar estado")
                }
            })
    }

    // GPS Tracking en background
    private fun startGPSTracking() {
        // Actualizar ubicación cada 5 minutos mientras está en tránsito
        timer.scheduleAtFixedRate(5 * 60 * 1000) {
            val location = getCurrentLocation()
            // Guardar en base de datos local
            saveLocationUpdate(location)

            // Sincronizar con servidor periódicamente
            syncLocationsToServer()
        }
    }
}
```

### 2. Panel Web del Cliente

#### Ver Estado en Tiempo Real

```typescript
// React/TypeScript Example
import { useEffect, useState } from 'react';
import { GoogleMap, Marker, Polyline } from '@react-google-maps/api';

interface TransportUpdate {
  id: number;
  status: string;
  statusDisplayName: string;
  timestamp: string;
  locationLatitude?: number;
  locationLongitude?: number;
  locationAddress?: string;
  notes?: string;
  updatedBy: string;
  photoUrl?: string;
}

export function OrderTrackingPage({ orderId }: { orderId: number }) {
  const [timeline, setTimeline] = useState<TransportUpdate[]>([]);
  const [currentStatus, setCurrentStatus] = useState<string>('');
  const [loading, setLoading] = useState(true);

  // Fetch timeline on mount
  useEffect(() => {
    fetchTimeline();

    // Poll for updates every 30 seconds
    const interval = setInterval(fetchTimeline, 30000);

    return () => clearInterval(interval);
  }, [orderId]);

  const fetchTimeline = async () => {
    try {
      const response = await fetch(`/api/order/${orderId}/transport/timeline`);
      const data = await response.json();

      if (data.success) {
        setTimeline(data.data);
        setCurrentStatus(data.data[0]?.status || 'PENDING');
      }
    } catch (error) {
      console.error('Error fetching timeline:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    const icons = {
      TRUCK_ASSIGNED: '🚚',
      EN_ROUTE_TO_WAREHOUSE: '🛣️',
      ARRIVED_AT_WAREHOUSE: '🏭',
      LOADING: '📦',
      LOADING_COMPLETED: '✅',
      EN_ROUTE_TO_DESTINATION: '🚛',
      ARRIVED_AT_DESTINATION: '📍',
      UNLOADING: '📤',
      DELIVERED: '🎉',
    };
    return icons[status] || '⏳';
  };

  const getStatusColor = (status: string) => {
    if (status === 'DELIVERED') return 'green';
    if (status.includes('ROUTE')) return 'blue';
    if (status.includes('LOADING') || status.includes('UNLOADING')) return 'orange';
    return 'gray';
  };

  return (
    <div className="tracking-container">
      {/* Status Timeline */}
      <div className="timeline">
        <h2>Historial de Transporte</h2>
        {timeline.map((update, index) => (
          <div key={update.id} className="timeline-item">
            <div className="timeline-marker" style={{ backgroundColor: getStatusColor(update.status) }}>
              {getStatusIcon(update.status)}
            </div>
            <div className="timeline-content">
              <h3>{update.statusDisplayName}</h3>
              <p className="timestamp">{new Date(update.timestamp).toLocaleString()}</p>
              {update.locationAddress && (
                <p className="address">📍 {update.locationAddress}</p>
              )}
              {update.notes && <p className="notes">{update.notes}</p>}
              {update.updatedBy && (
                <p className="updated-by">Por: {update.updatedBy}</p>
              )}
              {update.photoUrl && (
                <a href={update.photoUrl} target="_blank" rel="noopener noreferrer">
                  Ver foto 📷
                </a>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Map View */}
      <div className="map-container">
        <GoogleMap
          center={{
            lat: timeline[0]?.locationLatitude || -12.0464,
            lng: timeline[0]?.locationLongitude || -77.0428,
          }}
          zoom={12}
        >
          {/* Draw route */}
          <Polyline
            path={timeline
              .filter(t => t.locationLatitude && t.locationLongitude)
              .map(t => ({
                lat: t.locationLatitude!,
                lng: t.locationLongitude!,
              }))}
            options={{
              strokeColor: '#2196F3',
              strokeWeight: 3,
            }}
          />

          {/* Show markers */}
          {timeline.map((update, index) =>
            update.locationLatitude && update.locationLongitude ? (
              <Marker
                key={update.id}
                position={{
                  lat: update.locationLatitude,
                  lng: update.locationLongitude,
                }}
                label={{
                  text: getStatusIcon(update.status),
                  fontSize: '24px',
                }}
                title={update.statusDisplayName}
              />
            ) : null
          )}
        </GoogleMap>
      </div>

      {/* Current Status Card */}
      <div className="status-card">
        <h3>Estado Actual</h3>
        <div className="current-status">
          <span className="icon">{getStatusIcon(currentStatus)}</span>
          <span className="status-text">
            {timeline[0]?.statusDisplayName || 'Pendiente'}
          </span>
        </div>
        {timeline[0]?.timestamp && (
          <p className="last-update">
            Última actualización:{' '}
            {new Date(timeline[0].timestamp).toLocaleString()}
          </p>
        )}
      </div>
    </div>
  );
}
```

### 3. Dashboard de Administrador

#### Monitoreo de Múltiples Órdenes

```typescript
// Admin Dashboard
export function AdminTransportDashboard() {
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    fetchActiveTransports();
    const interval = setInterval(fetchActiveTransports, 60000); // 1 minute
    return () => clearInterval(interval);
  }, []);

  const fetchActiveTransports = async () => {
    const response = await fetch('/api/order?status=IN_PROGRESS');
    const data = await response.json();
    setOrders(data.data);
  };

  const getOrdersByTransportStatus = (status: string) => {
    return orders.filter(o => o.transportStatus === status);
  };

  return (
    <div className="dashboard">
      <h1>Panel de Transporte en Vivo</h1>

      {/* Stats Cards */}
      <div className="stats-grid">
        <StatCard
          title="En Tránsito"
          count={
            getOrdersByTransportStatus('EN_ROUTE_TO_WAREHOUSE').length +
            getOrdersByTransportStatus('EN_ROUTE_TO_DESTINATION').length
          }
          icon="🚛"
          color="blue"
        />
        <StatCard
          title="Cargando"
          count={getOrdersByTransportStatus('LOADING').length}
          icon="📦"
          color="orange"
        />
        <StatCard
          title="Descargando"
          count={getOrdersByTransportStatus('UNLOADING').length}
          icon="📤"
          color="purple"
        />
        <StatCard
          title="Por Entregar Hoy"
          count={orders.filter(o =>
            isToday(o.projectedDeliveryDate) && o.transportStatus !== 'DELIVERED'
          ).length}
          icon="📅"
          color="green"
        />
      </div>

      {/* Live Map */}
      <div className="live-map">
        <h2>Tracking en Vivo</h2>
        <GoogleMap>
          {orders.map(order =>
            order.lastLocation ? (
              <Marker
                key={order.id}
                position={{
                  lat: order.lastLocation.latitude,
                  lng: order.lastLocation.longitude,
                }}
                label={order.truck?.licensePlate || order.id.toString()}
                onClick={() => showOrderDetails(order)}
              />
            ) : null
          )}
        </GoogleMap>
      </div>

      {/* Orders List */}
      <div className="orders-list">
        <h2>Órdenes Activas</h2>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Cliente</th>
              <th>Camión</th>
              <th>Estado de Transporte</th>
              <th>Última Ubicación</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(order => (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{order.client.businessName}</td>
                <td>{order.truck?.licensePlate}</td>
                <td>
                  <StatusBadge status={order.transportStatus} />
                </td>
                <td>{order.lastLocation?.address || '-'}</td>
                <td>
                  <button onClick={() => viewTimeline(order.id)}>
                    Ver Timeline
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

### 4. Notificaciones Push

#### Notificar Cambios de Estado

```java
// Spring Boot Service
@Service
@RequiredArgsConstructor
public class TransportNotificationService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @EventListener
    public void onTransportStatusChanged(TransportStatusUpdateEvent event) {
        Order order = event.getOrder();
        TransportStatus newStatus = event.getNewStatus();
        TransportStatusUpdate update = event.getUpdate();

        // Notificar al cliente
        notifyClient(order, newStatus, update);

        // Notificar a admins si es crítico
        if (isCriticalStatus(newStatus)) {
            notifyAdmins(order, newStatus, update);
        }
    }

    private void notifyClient(Order order, TransportStatus status, TransportStatusUpdate update) {
        User clientUser = order.getClient().getUser();

        String title = getNotificationTitle(status);
        String body = getNotificationBody(status, update);

        // Push notification
        notificationService.sendPushNotification(
            clientUser.getId(),
            title,
            body,
            Map.of(
                "orderId", order.getId().toString(),
                "status", status.name(),
                "type", "TRANSPORT_UPDATE"
            )
        );

        // Email notification
        if (shouldSendEmail(status)) {
            notificationService.sendEmail(
                clientUser.getEmail(),
                title,
                generateEmailTemplate(order, status, update)
            );
        }

        // SMS notification para estados críticos
        if (isCriticalStatus(status)) {
            notificationService.sendSMS(
                order.getClient().getPhone(),
                body
            );
        }
    }

    private String getNotificationTitle(TransportStatus status) {
        return switch (status) {
            case TRUCK_ASSIGNED -> "🚚 Camión asignado";
            case EN_ROUTE_TO_WAREHOUSE -> "🛣️ Camión en camino al almacén";
            case LOADING -> "📦 Cargando su mercancía";
            case EN_ROUTE_TO_DESTINATION -> "🚛 En camino a su dirección";
            case ARRIVED_AT_DESTINATION -> "📍 Hemos llegado";
            case DELIVERED -> "🎉 Entrega completada";
            default -> "📢 Actualización de envío";
        };
    }

    private String getNotificationBody(TransportStatus status, TransportStatusUpdate update) {
        StringBuilder body = new StringBuilder();
        body.append(status.getDescription());

        if (update.getNotes() != null) {
            body.append("\n\nNotas: ").append(update.getNotes());
        }

        if (update.getLocationAddress() != null) {
            body.append("\n📍 ").append(update.getLocationAddress());
        }

        return body.toString();
    }

    private boolean isCriticalStatus(TransportStatus status) {
        return status == TransportStatus.ARRIVED_AT_DESTINATION
            || status == TransportStatus.DELIVERED
            || status == TransportStatus.LOADING
            || status == TransportStatus.UNLOADING;
    }

    private boolean shouldSendEmail(TransportStatus status) {
        return status == TransportStatus.TRUCK_ASSIGNED
            || status == TransportStatus.EN_ROUTE_TO_DESTINATION
            || status == TransportStatus.DELIVERED;
    }
}
```

### 5. Reportes y Analytics

#### Calcular KPIs de Transporte

```java
@Service
@RequiredArgsConstructor
public class TransportAnalyticsService {

    private final TransportStatusUpdateRepository transportStatusUpdateRepository;
    private final OrderRepository orderRepository;

    /**
     * Calcula el tiempo promedio que se tarda en cada estado
     */
    public Map<TransportStatus, Duration> getAverageTimePerStatus() {
        Map<TransportStatus, Duration> averages = new EnumMap<>(TransportStatus.class);

        for (TransportStatus status : TransportStatus.values()) {
            List<Duration> durations = calculateDurationsForStatus(status);
            if (!durations.isEmpty()) {
                Duration average = calculateAverage(durations);
                averages.put(status, average);
            }
        }

        return averages;
    }

    /**
     * Calcula el tiempo total de un envío desde asignación hasta entrega
     */
    public Duration getTotalDeliveryTime(Long orderId) {
        List<TransportStatusUpdate> updates =
            transportStatusUpdateRepository.findByOrder_IdOrderByTimestampAsc(orderId);

        if (updates.size() < 2) {
            return Duration.ZERO;
        }

        LocalDateTime start = updates.get(0).getTimestamp();
        LocalDateTime end = updates.get(updates.size() - 1).getTimestamp();

        return Duration.between(start, end);
    }

    /**
     * Calcula la distancia recorrida basada en coordenadas GPS
     */
    public double getTotalDistanceTraveled(Long orderId) {
        List<TransportStatusUpdate> updates =
            transportStatusUpdateRepository.findByOrderIdWithLocation(orderId);

        double totalDistance = 0.0;

        for (int i = 1; i < updates.size(); i++) {
            TransportStatusUpdate prev = updates.get(i - 1);
            TransportStatusUpdate current = updates.get(i);

            double distance = calculateDistance(
                prev.getLocationLatitude(), prev.getLocationLongitude(),
                current.getLocationLatitude(), current.getLocationLongitude()
            );

            totalDistance += distance;
        }

        return totalDistance; // en kilómetros
    }

    /**
     * Fórmula de Haversine para calcular distancia entre dos puntos GPS
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Identifica órdenes con retrasos
     */
    public List<Order> getDelayedOrders() {
        LocalDateTime now = LocalDateTime.now();

        return orderRepository.findAll().stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.IN_PROGRESS)
            .filter(o -> o.getProjectedDeliveryDate().isBefore(now))
            .filter(o -> o.getTransportStatus() != TransportStatus.DELIVERED)
            .collect(Collectors.toList());
    }

    /**
     * Calcula el % de entregas a tiempo
     */
    public double getOnTimeDeliveryRate(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> deliveredOrders = orderRepository
            .findByOrderStatusAndRealDeliveryDateBetween(
                OrderStatus.DELIVERED, startDate, endDate);

        if (deliveredOrders.isEmpty()) {
            return 0.0;
        }

        long onTime = deliveredOrders.stream()
            .filter(o -> o.getRealDeliveryDate().isBefore(o.getProjectedDeliveryDate())
                || o.getRealDeliveryDate().isEqual(o.getProjectedDeliveryDate()))
            .count();

        return (double) onTime / deliveredOrders.size() * 100;
    }
}
```

## 🔐 Seguridad y Permisos

### Ejemplo de Autorización con Roles

```java
@RestController
@RequestMapping("/api/order/{orderId}/transport")
public class TransportStatusController {

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<GenericResponse> updateTransportStatus(
        @PathVariable Long orderId,
        @Valid @RequestBody TransportStatusUpdateRequest request,
        Authentication authentication) {

        // Validar que el driver es el asignado a la orden
        if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"))) {

            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

            User currentUser = (User) authentication.getPrincipal();
            Long driverId = order.getTruck().getDriver().getUser().getId();

            if (!currentUser.getId().equals(driverId)) {
                throw new AccessDeniedException("Solo el conductor asignado puede actualizar el estado");
            }
        }

        // Proceder con actualización
        return ResponseEntity.ok(transportStatusService.updateTransportStatus(...));
    }
}
```

## 🎯 Conclusión

Este sistema te proporciona:

✅ **Tracking granular** de cada etapa del transporte
✅ **Historial completo** con timestamps, ubicaciones y evidencia fotográfica
✅ **Validaciones robustas** de transiciones de estado
✅ **API RESTful** fácil de integrar con apps móviles y web
✅ **Notificaciones en tiempo real** para clientes y administradores
✅ **Analytics** para medir KPIs y mejorar operaciones

¡Tu sistema de transporte ahora tiene visibilidad completa! 🚀
