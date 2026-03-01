# Quick Reference - Dashboard API

## Ubicaciones de Archivos

### DTOs
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

### Servicio
```
src/main/java/com/tupack/palletsortingapi/order/application/
└── DashboardService.java
```

### Controlador
```
src/main/java/com/tupack/palletsortingapi/order/infrastructure/inbound/controller/
└── DashboardController.java
```

## Endpoints Rápidos

| # | Método | Ruta | Descripción |
|---|--------|------|-------------|
| 1 | GET | `/api/dashboard/stats` | Estadísticas generales |
| 2 | GET | `/api/dashboard/pending-orders` | Órdenes pendientes |
| 3 | GET | `/api/dashboard/orders-by-client` | Por cliente |
| 4 | GET | `/api/dashboard/orders-by-driver` | Por conductor |
| 5 | GET | `/api/dashboard/orders-by-truck` | Por camión |
| 6 | GET | `/api/dashboard/orders-by-status` | Por estado |
| 7 | GET | `/api/dashboard/performance-metrics` | Métricas |

## Métodos del Servicio

```java
// Endpoint 1
DashboardStatsDTO getStats()

// Endpoint 2
List<PendingOrderDTO> getPendingOrders(Integer limit)

// Endpoint 3
List<OrdersByClientDTO> getOrdersByClient()

// Endpoint 4
List<OrdersByDriverDTO> getOrdersByDriver()

// Endpoint 5
List<OrdersByTruckDTO> getOrdersByTruck()

// Endpoint 6
List<OrdersByStatusDTO> getOrdersByStatus()

// Endpoint 7
PerformanceMetricsDTO getPerformanceMetrics()
```

## Importaciones Clave

### Para DTOs
```java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
```

### Para Servicio
```java
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dto.*;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.OrderRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.ClientRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.DriverRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
```

### Para Controlador
```java
import com.tupack.palletsortingapi.order.application.DashboardService;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
```

## Anotaciones Usadas

### DTOs
- `@Data` - Genera getters, setters, toString, equals, hashCode
- `@Builder` - Patrón builder para construcción de objetos
- `@NoArgsConstructor` - Constructor sin argumentos
- `@AllArgsConstructor` - Constructor con todos los argumentos

### Servicio
- `@Service` - Marca como componente de servicio de Spring
- `@RequiredArgsConstructor` - Genera constructor con campos finales

### Controlador
- `@RestController` - Marca como controlador REST
- `@RequestMapping("/api/dashboard")` - Ruta base
- `@RequiredArgsConstructor` - Inyección de dependencias
- `@GetMapping("/ruta")` - Mapea GET a ruta específica
- `@RequestParam` - Parámetro query

## Relaciones de Datos

```
Order
├── client (Client)
│   └── user (User) → getFirstName() + getLastName()
├── truckOrder (TruckOrder)
│   └── truck (Truck)
│       ├── licensePlate (String)
│       └── driver (Driver)
│           └── user (User) → getFirstName() + getLastName()
└── orderStatus (OrderStatus)
```

## Repositorios Inyectados

```java
private final OrderRepository orderRepository;
private final ClientRepository clientRepository;
private final DriverRepository driverRepository;
private final TruckRepository truckRepository;
```

## Conversiones de Tipo

```java
// BigDecimal a Double
order.getAmount().doubleValue()

// LocalDateTime a LocalDate
order.getPickupDate().toLocalDate()

// ID a String
order.getId().toString()

// Enum a String
order.getOrderStatus().name()
```

## Operaciones Comunes

### Filtro
```java
.filter(order -> order.getOrderStatus() == OrderStatus.PENDING)
```

### Mapeo
```java
.map(order -> PendingOrderDTO.builder()...build())
```

### Agrupamiento
```java
.collect(Collectors.groupingBy(order -> order.getClient(), Collectors.counting()))
```

### Límite
```java
.limit(actualLimit)
```

## Manejo de Nulos

```java
// Verificar antes de acceder
if (order.getClient() != null && order.getClient().getUser() != null) {
    String name = order.getClient().getUser().getFirstName();
}

// Usar ternario
String clientName = order.getClient() != null ? 
    order.getClient().getClientName() : "";

// Usar Optional
order.getClient()
    .map(Client::getClientName)
    .orElse("");
```

## Testing Rápido

```bash
# curl
curl http://localhost:8080/api/dashboard/stats

# Postman
Crear nueva solicitud GET
URL: http://localhost:8080/api/dashboard/stats
Click Send
```

## Documentación

- 📄 DASHBOARD_API_GUIDE.md - Guía completa
- 📄 JSON_EXAMPLES.md - Ejemplos de respuestas
- 📄 VERIFICATION.md - Checklist de verificación
- 📄 CHANGES_SUMMARY.md - Resumen de cambios

## Notas Importantes

✅ Los repositorios ya existen, no necesitan ser creados
✅ Las entidades (Order, Client, Driver, Truck) ya existen
✅ Los DTOs son nuevos y específicos para este API
✅ El servicio usa solo `findAll()` - optimizar si es necesario para grandes volúmenes
✅ El parámetro `limit` es opcional con default de 10

## Compilación

```bash
cd D:\Proyectos\TUPACK\pallet-sorting-api
mvn clean compile
mvn clean package
mvn spring-boot:run
```

## Archivo POM Requerido

El proyecto ya tiene Spring Boot 3.5.0 configurado con:
- Spring Data JPA
- Lombok
- Jackson (para serialización JSON)

No se requieren dependencias adicionales.

