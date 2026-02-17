package com.tupack.palletsortingapi.notification.infrastructure.listener;

import com.tupack.palletsortingapi.notification.application.dto.CreateNotificationDTO;
import com.tupack.palletsortingapi.notification.application.service.NotificationService;
import com.tupack.palletsortingapi.notification.domain.enums.NotificationType;
import com.tupack.palletsortingapi.notification.domain.event.OrderCreatedEvent;
import com.tupack.palletsortingapi.notification.domain.event.OrderStatusChangedEvent;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

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
            List<String> adminIds = notificationService.getUserIdsByRole("ROLE_ADMIN");
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

    private NotificationType mapStatusToNotificationType(OrderStatus status) {
        return switch (status) {
            case APPROVED -> NotificationType.ORDER_APPROVED;
            case DENIED -> NotificationType.ORDER_DENIED;
            case IN_PROGRESS -> NotificationType.TRANSPORT_STARTED;
            case DELIVERED -> NotificationType.TRANSPORT_DELIVERED;
            default -> NotificationType.ORDER_STATUS_CHANGED;
        };
    }

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

    private String getNotificationMessage(String orderId, OrderStatus status) {
        return switch (status) {
            case APPROVED -> "Tu orden #" + orderId + " ha sido aprobada";
            case DENIED -> "Tu orden #" + orderId + " ha sido denegada";
            case IN_PROGRESS -> "Tu orden #" + orderId + " está en tránsito";
            case DELIVERED -> "Tu orden #" + orderId + " ha sido entregada";
            default -> "El estado de tu orden #" + orderId + " ha cambiado a " + status;
        };
    }

    private Map<String, Object> buildOrderMetadata(com.tupack.palletsortingapi.order.domain.Order order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", order.getId());
        metadata.put("orderStatus", order.getOrderStatus() != null ? order.getOrderStatus().toString() : null);
        return metadata;
    }
}
