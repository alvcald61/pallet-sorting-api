package com.tupack.palletsortingapi.notification.infrastructure.listener;

import com.tupack.palletsortingapi.notification.application.dto.CreateNotificationDTO;
import com.tupack.palletsortingapi.notification.application.service.NotificationService;
import com.tupack.palletsortingapi.notification.domain.enums.NotificationType;
import com.tupack.palletsortingapi.notification.domain.event.OrderCreatedEvent;
import com.tupack.palletsortingapi.notification.domain.event.OrderStatusChangedEvent;
import com.tupack.palletsortingapi.notification.domain.event.TransportStatusUpdatedEvent;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.domain.enums.TransportStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener de eventos de órdenes que genera notificaciones automáticas.
 * <p>
 * Este componente implementa el patrón Event-Driven Architecture, escuchando
 * eventos del dominio de órdenes (OrderCreatedEvent, OrderStatusChangedEvent)
 * y generando notificaciones correspondientes para los usuarios involucrados.
 * </p>
 *
 * <h2>Arquitectura Event-Driven</h2>
 * <p>
 * <strong>Publisher:</strong> OrderService publica eventos usando ApplicationEventPublisher
 * <br>
 * <strong>Listener:</strong> Este componente escucha eventos con @EventListener
 * <br>
 * <strong>Side Effects:</strong> Genera notificaciones en BD y envía push vía OneSignal
 * </p>
 *
 * <h2>Ejecución Asíncrona</h2>
 * <p>
 * Todos los event handlers están anotados con @Async, lo que significa:
 * <ul>
 *   <li>No bloquean el thread principal de OrderService</li>
 *   <li>Orden se guarda primero, luego notificaciones se procesan en background</li>
 *   <li>Si el envío de notificaciones falla, la orden ya está guardada (no se hace rollback)</li>
 *   <li>Requiere @EnableAsync en la aplicación principal</li>
 * </ul>
 * </p>
 *
 * <h2>Manejo de Errores</h2>
 * <p>
 * Cada listener tiene try-catch que logea errores pero no los propaga.
 * Esto previene que errores en notificaciones afecten las operaciones principales.
 * Las notificaciones son consideradas "best effort" - si fallan, no crashean el sistema.
 * </p>
 *
 * <h2>Destinatarios de Notificaciones</h2>
 * <ul>
 *   <li><strong>OrderCreated:</strong> Creador de la orden + todos los ADMINs</li>
 *   <li><strong>StatusChanged:</strong> Creador + driver asignado (si status=APPROVED)</li>
 *   <li><strong>TransportStarted/Delivered:</strong> Creador de la orden</li>
 * </ul>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see OrderCreatedEvent
 * @see OrderStatusChangedEvent
 * @see NotificationService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    /**
     * Servicio de notificaciones para crear y enviar notificaciones.
     */
    private final NotificationService notificationService;

    /**
     * Maneja el evento de creación de una orden.
     * <p>
     * Se ejecuta asíncronamente después de que OrderService guarda una nueva orden.
     * Genera dos tipos de notificaciones:
     * <ol>
     *   <li><strong>Para el creador:</strong> "Orden Creada" - Confirmación de que su orden fue recibida</li>
     *   <li><strong>Para todos los ADMINs:</strong> "Nueva Orden Pendiente" - Alerta de que requiere aprobación</li>
     * </ol>
     * </p>
     *
     * <h3>Flujo de Ejecución:</h3>
     * <pre>
     * 1. Cliente hace POST /api/order
     * 2. OrderService.createOrder() guarda la orden
     * 3. OrderService publica OrderCreatedEvent
     * 4. Este listener detecta el evento (asíncrono)
     * 5. Notifica al creador (base de datos + push)
     * 6. Consulta todos los usuarios con rol ADMIN
     * 7. Notifica a cada ADMIN (batch save + batch push)
     * </pre>
     *
     * <h3>Metadatos Incluidos:</h3>
     * <p>
     * Cada notificación incluye metadata con:
     * <ul>
     *   <li>orderId - ID de la orden creada</li>
     *   <li>orderStatus - Estado actual de la orden</li>
     * </ul>
     * Esta metadata permite al frontend construir deep links: /order/123
     * </p>
     *
     * <h3>Manejo de Errores:</h3>
     * <p>
     * Si falla el envío de notificaciones (ej: OneSignal caído, BD error):
     * <ul>
     *   <li>Se logea el error completo</li>
     *   <li>La orden ya fue guardada (no se revierte)</li>
     *   <li>No se lanzan excepciones al caller</li>
     * </ul>
     * </p>
     *
     * <h3>Ejemplo de Push Notification Generada:</h3>
     * <pre>
     * Título: "Nueva Orden Pendiente"
     * Mensaje: "Nueva orden #123 requiere aprobación"
     * Data: {
     *   "entityType": "ORDER",
     *   "entityId": "123",
     *   "orderId": 123,
     *   "orderStatus": "PRE_APPROVED"
     * }
     * </pre>
     *
     * @param event Evento publicado por OrderService conteniendo la orden recién creada
     */
    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            var order = event.getOrder();
            log.info("Order created event received for order: {}", order.getId());

            // Notify creator
            if (order.getCreatedBy() != null) {
                CreateNotificationDTO creatorNotification = CreateNotificationDTO.builder()
                        .title("Orden Creada")
                        .message("Tu orden #" + order.getId() + " ha sido creada exitosamente")
                        .type(NotificationType.ORDER_CREATED)
                        .relatedEntityType("ORDER")
                        .relatedEntityId(String.valueOf(order.getId()))
                        .userId(order.getCreatedBy())
                        .metadata(buildOrderMetadata(order))
                        .build();

                notificationService.createNotification(creatorNotification);
            }

            // Notify all ADMINs
            List<String> adminIds = notificationService.getUserIdsByRole("ADMIN");
            if (!adminIds.isEmpty()) {
                CreateNotificationDTO adminNotification = CreateNotificationDTO.builder()
                        .title("Nueva Orden Pendiente")
                        .message("Nueva orden #" + order.getId() + " requiere aprobación")
                        .type(NotificationType.ORDER_CREATED)
                        .relatedEntityType("ORDER")
                        .relatedEntityId(String.valueOf(order.getId()))
                        .metadata(buildOrderMetadata(order))
                        .build();

                notificationService.createNotificationForMultipleUsers(adminIds, adminNotification);
            }

            log.info("Notifications sent for order created: {}", order.getId());
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Maneja el evento de cambio de estado de una orden.
     * <p>
     * Se ejecuta asíncronamente cada vez que el estado de una orden cambia.
     * Genera notificaciones contextuales basadas en el nuevo estado, enviando
     * a diferentes destinatarios según el caso.
     * </p>
     *
     * <h3>Destinatarios Según Estado:</h3>
     * <table border="1">
     *   <tr>
     *     <th>Estado</th>
     *     <th>Título</th>
     *     <th>Destinatarios</th>
     *   </tr>
     *   <tr>
     *     <td>APPROVED</td>
     *     <td>"Orden Aprobada"</td>
     *     <td>Creador + Driver asignado</td>
     *   </tr>
     *   <tr>
     *     <td>DENIED</td>
     *     <td>"Orden Denegada"</td>
     *     <td>Creador</td>
     *   </tr>
     *   <tr>
     *     <td>IN_PROGRESS</td>
     *     <td>"Transporte Iniciado"</td>
     *     <td>Creador</td>
     *   </tr>
     *   <tr>
     *     <td>DELIVERED</td>
     *     <td>"Orden Entregada"</td>
     *     <td>Creador</td>
     *   </tr>
     *   <tr>
     *     <td>Otros</td>
     *     <td>"Estado de Orden Actualizado"</td>
     *     <td>Creador</td>
     *   </tr>
     * </table>
     *
     * <h3>Lógica Especial para APPROVED:</h3>
     * <p>
     * Cuando una orden es aprobada:
     * <ol>
     *   <li>Notifica al creador: "Tu orden #123 ha sido aprobada"</li>
     *   <li>Si hay driver asignado, notifica al driver: "Se te ha asignado la orden #123"</li>
     * </ol>
     * Esto permite que el driver reciba una alerta inmediata de su nueva asignación.
     * </p>
     *
     * <h3>Flujo de Ejecución:</h3>
     * <pre>
     * 1. Admin hace PATCH /api/order/123/status → APPROVED
     * 2. OrderService.updateOrderStatus() actualiza la orden
     * 3. OrderService publica OrderStatusChangedEvent
     * 4. Este listener detecta el evento (asíncrono)
     * 5. Mapea status → NotificationType
     * 6. Genera título y mensaje según el estado
     * 7. Notifica al creador
     * 8. Si APPROVED, notifica al driver
     * 9. Si IN_PROGRESS/DELIVERED, envía notificación de transporte
     * </pre>
     *
     * <h3>Mapeo de Estados a NotificationType:</h3>
     * <ul>
     *   <li>APPROVED → ORDER_APPROVED</li>
     *   <li>DENIED → ORDER_DENIED</li>
     *   <li>IN_PROGRESS → TRANSPORT_STARTED</li>
     *   <li>DELIVERED → TRANSPORT_DELIVERED</li>
     *   <li>Otros → ORDER_STATUS_CHANGED</li>
     * </ul>
     *
     * <h3>Ejemplo de Notificación Generada (APPROVED):</h3>
     * <pre>
     * Para el creador:
     *   Título: "Orden Aprobada"
     *   Mensaje: "Tu orden #123 ha sido aprobada"
     *   Tipo: ORDER_APPROVED
     *
     * Para el driver:
     *   Título: "Nueva Orden Asignada"
     *   Mensaje: "Se te ha asignado la orden #123"
     *   Tipo: ORDER_APPROVED
     * </pre>
     *
     * @param event Evento publicado por OrderService conteniendo la orden,
     *              el estado anterior y el nuevo estado
     */
    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            var order = event.getOrder();
            var newStatus = event.getNewStatus();
            log.info("Order status changed event received for order: {} to status: {}", order.getId(), newStatus);

            NotificationType notificationType = mapStatusToNotificationType(newStatus);
            String title = getNotificationTitle(newStatus);
            String message = getNotificationMessage(String.valueOf(order.getId()), newStatus);

            // Notify order creator
            if (order.getCreatedBy() != null) {
                CreateNotificationDTO creatorNotification = CreateNotificationDTO.builder()
                        .title(title)
                        .message(message)
                        .type(notificationType)
                        .relatedEntityType("ORDER")
                        .relatedEntityId(String.valueOf(order.getId()))
                        .userId(order.getCreatedBy())
                        .metadata(buildOrderMetadata(order))
                        .build();

                notificationService.createNotification(creatorNotification);
            }

            // If approved, notify assigned driver
            if (newStatus == OrderStatus.APPROVED && order.getTruck().getDriver() != null) {
                CreateNotificationDTO driverNotification = CreateNotificationDTO.builder()
                        .title("Nueva Orden Asignada")
                        .message("Se te ha asignado la orden #" + order.getId())
                        .type(NotificationType.ORDER_APPROVED)
                        .relatedEntityType("ORDER")
                        .relatedEntityId(String.valueOf(order.getId()))
                        .userId(String.valueOf(order.getTruck().getDriver().getDriverId()))
                        .metadata(buildOrderMetadata(order))
                        .build();

                notificationService.createNotification(driverNotification);
            }

            // If transport started or delivered, notify creator
            if (newStatus == OrderStatus.IN_PROGRESS) {
                sendTransportNotification(order, "Transporte Iniciado", "Tu orden #" + order.getId() + " está en tránsito", NotificationType.TRANSPORT_STARTED);
            } else if (newStatus == OrderStatus.DELIVERED) {
                sendTransportNotification(order, "Orden Entregada", "Tu orden #" + order.getId() + " ha sido entregada", NotificationType.TRANSPORT_DELIVERED);
            }

            log.info("Notifications sent for order status change: {}", order.getId());
        } catch (Exception e) {
            log.error("Error processing OrderStatusChangedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía una notificación de transporte al creador de la orden.
     * <p>
     * Método auxiliar para reducir duplicación de código en notificaciones
     * relacionadas con transporte (IN_PROGRESS, DELIVERED).
     * </p>
     *
     * @param order   Orden para la cual se envía la notificación
     * @param title   Título de la notificación (ej: "Transporte Iniciado")
     * @param message Mensaje descriptivo
     * @param type    Tipo de notificación (TRANSPORT_STARTED o TRANSPORT_DELIVERED)
     */
    private void sendTransportNotification(com.tupack.palletsortingapi.order.domain.Order order, String title, String message, NotificationType type) {
        if (order.getCreatedBy() != null) {
            CreateNotificationDTO notification = CreateNotificationDTO.builder()
                    .title(title)
                    .message(message)
                    .type(type)
                    .relatedEntityType("ORDER")
                    .relatedEntityId(String.valueOf(order.getId()))
                    .userId(order.getCreatedBy())
                    .metadata(buildOrderMetadata(order))
                    .build();

            notificationService.createNotification(notification);
        }
    }

    /**
     * Mapea un OrderStatus a su NotificationType correspondiente.
     * <p>
     * Determina el tipo de notificación basado en el estado de la orden,
     * lo cual afecta el color del badge y el icono en el frontend.
     * </p>
     *
     * <h3>Mapeo:</h3>
     * <ul>
     *   <li>APPROVED → ORDER_APPROVED (verde)</li>
     *   <li>DENIED → ORDER_DENIED (rojo)</li>
     *   <li>IN_PROGRESS → TRANSPORT_STARTED (azul)</li>
     *   <li>DELIVERED → TRANSPORT_DELIVERED (verde)</li>
     *   <li>Otros → ORDER_STATUS_CHANGED (gris)</li>
     * </ul>
     *
     * @param status Estado actual de la orden
     * @return NotificationType correspondiente al estado
     */
    private NotificationType mapStatusToNotificationType(OrderStatus status) {
        return switch (status) {
            case APPROVED -> NotificationType.ORDER_APPROVED;
            case DENIED -> NotificationType.ORDER_DENIED;
            case IN_PROGRESS -> NotificationType.TRANSPORT_STARTED;
            case DELIVERED -> NotificationType.TRANSPORT_DELIVERED;
            default -> NotificationType.ORDER_STATUS_CHANGED;
        };
    }

    /**
     * Genera el título de la notificación basado en el estado de la orden.
     * <p>
     * Títulos concisos en español para mostrar en el UI.
     * Se muestra prominentemente en la notificación push y en el menú.
     * </p>
     *
     * @param status Estado actual de la orden
     * @return Título localizado para la notificación
     */
    private String getNotificationTitle(OrderStatus status) {
        return switch (status) {
            case APPROVED -> "Orden Aprobada";
            case DENIED -> "Orden Denegada";
            case IN_PROGRESS -> "Transporte Iniciado";
            case DELIVERED -> "Orden Entregada";
            case PRE_APPROVED -> "Orden Pendiente";
            default -> "Estado de Orden Actualizado";
        };
    }

    /**
     * Genera el mensaje descriptivo de la notificación basado en el estado.
     * <p>
     * Mensajes más extensos que proporcionan contexto adicional al usuario.
     * Se muestran en el cuerpo de la notificación push y en el menú.
     * </p>
     *
     * @param orderId ID de la orden para personalizar el mensaje
     * @param status  Estado actual de la orden
     * @return Mensaje localizado y personalizado para la notificación
     */
    private String getNotificationMessage(String orderId, OrderStatus status) {
        return switch (status) {
            case APPROVED -> "Tu orden #" + orderId + " ha sido aprobada";
            case DENIED -> "Tu orden #" + orderId + " ha sido denegada";
            case IN_PROGRESS -> "Tu orden #" + orderId + " está en tránsito";
            case DELIVERED -> "Tu orden #" + orderId + " ha sido entregada";
            default -> "El estado de tu orden #" + orderId + " ha cambiado a " + status;
        };
    }

    /**
     * Construye el mapa de metadata para incluir en la notificación.
     * <p>
     * La metadata se guarda en el campo JSON de la tabla notifications
     * y se incluye en el payload de push notification.
     * </p>
     *
     * <h3>Campos Incluidos:</h3>
     * <ul>
     *   <li><strong>orderId:</strong> ID de la orden (Long)</li>
     *   <li><strong>orderStatus:</strong> Estado actual como String (puede ser null)</li>
     * </ul>
     *
     * <h3>Uso de Metadata en Frontend:</h3>
     * <pre>
     * {@code
     * // Al hacer click en notificación
     * const handleClick = (notification) => {
     *   if (notification.relatedEntityType === "ORDER") {
     *     router.push(`/order/${notification.relatedEntityId}`);
     *   }
     * };
     *
     * // Metadata adicional disponible en notification.metadata
     * console.log(notification.metadata.orderStatus); // "APPROVED"
     * }
     * </pre>
     *
     * @param order Orden para extraer metadata
     * @return Map con metadata en formato clave-valor
     */
    /**
     * Maneja el evento de cambio de estado de transporte de una orden.
     * <p>
     * Se ejecuta asíncronamente cuando el driver actualiza el estado de transporte
     * vía PATCH /api/order/{orderId}/transport/status/quick.
     * Notifica al creador de la orden sobre el nuevo estado del transporte.
     * El estado DELIVERED es excluido porque ya se notifica via OrderStatusChangedEvent.
     * </p>
     *
     * @param event Evento publicado por TransportStatusService
     */
    @Async
    @EventListener
    public void onTransportStatusUpdated(TransportStatusUpdatedEvent event) {
        try {
            var order = event.getOrder();
            var newStatus = event.getNewTransportStatus();
            log.info("Transport status updated event received for order: {} to status: {}", order.getId(), newStatus);

            // DELIVERED is already handled by OrderStatusChangedEvent via updateOrderStatus
            if (newStatus == TransportStatus.DELIVERED) {
                return;
            }

//            if (order.getCreatedBy() == null) {
//                return;
//            }

            String title = getTransportStatusTitle(newStatus);
            String message = "Tu orden #" + order.getId() + " - " + getTransportStatusMessage(newStatus);

            Map<String, Object> metadata = buildOrderMetadata(order);
            metadata.put("transportStatus", newStatus.toString());

            CreateNotificationDTO notification = CreateNotificationDTO.builder()
                    .title(title)
                    .message(message)
                    .type(NotificationType.TRANSPORT_STARTED)
                    .relatedEntityType("ORDER")
                    .relatedEntityId(String.valueOf(order.getId()))
                    .userId("tupack_" + order.getClient().getUser().getId().toString())
                    .metadata(metadata)
                    .build();

            notificationService.createNotification(notification);
            log.info("Transport status notification sent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Error processing TransportStatusUpdatedEvent: {}", e.getMessage(), e);
        }
    }

    private String getTransportStatusTitle(TransportStatus status) {
        return switch (status) {
            case EN_ROUTE_TO_WAREHOUSE -> "Camión en Ruta al Almacén";
            case ARRIVED_AT_WAREHOUSE -> "Camión Llegó al Almacén";
            case LOADING -> "Carga en Progreso";
            case LOADING_COMPLETED -> "Carga Completada";
            case EN_ROUTE_TO_DESTINATION -> "Camión en Ruta a tu Destino";
            case ARRIVED_AT_DESTINATION -> "Camión Llegó al Destino";
            case UNLOADING -> "Descarga en Progreso";
            case UNLOADING_COMPLETED -> "Descarga Completada";
            default -> "Estado de Transporte Actualizado";
        };
    }

    private String getTransportStatusMessage(TransportStatus status) {
        return switch (status) {
            case EN_ROUTE_TO_WAREHOUSE -> "El camión está en ruta al almacén";
            case ARRIVED_AT_WAREHOUSE -> "El camión llegó al almacén";
            case LOADING -> "Se está cargando tu mercancía";
            case LOADING_COMPLETED -> "La carga de tu mercancía está completa";
            case EN_ROUTE_TO_DESTINATION -> "El camión está en ruta a tu destino";
            case ARRIVED_AT_DESTINATION -> "El camión llegó a tu destino";
            case UNLOADING -> "Se está descargando tu mercancía";
            case UNLOADING_COMPLETED -> "La descarga de tu mercancía está completa";
            default -> "El estado de transporte cambió a " + status.getDisplayName();
        };
    }

    private Map<String, Object> buildOrderMetadata(com.tupack.palletsortingapi.order.domain.Order order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", order.getId());
        metadata.put("orderStatus", order.getOrderStatus() != null ? order.getOrderStatus().toString() : null);
        return metadata;
    }
}
