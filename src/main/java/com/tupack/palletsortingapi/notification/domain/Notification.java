package com.tupack.palletsortingapi.notification.domain;

import com.tupack.palletsortingapi.notification.domain.enums.NotificationType;
import com.tupack.palletsortingapi.order.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidad que representa una notificación en el sistema.
 * <p>
 * Las notificaciones se generan automáticamente cuando ocurren eventos relevantes
 * (creación de órdenes, cambios de estado, etc.) y se envían a los usuarios
 * correspondientes tanto en la base de datos como vía push notification.
 * </p>
 * <p>
 * Extiende de {@link BaseEntity} para heredar campos comunes como id, createdAt, updatedAt.
 * </p>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see NotificationType
 * @see BaseEntity
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_read_created", columnList = "user_id, is_read, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    /**
     * Título de la notificación.
     * Se muestra como encabezado en el UI y en push notifications.
     * Ejemplos: "Orden Creada", "Orden Aprobada", "Transporte Iniciado"
     */
    @Column(nullable = false)
    private String title;

    /**
     * Mensaje descriptivo de la notificación.
     * Proporciona detalles adicionales sobre el evento.
     * Ejemplo: "Tu orden #123 ha sido creada exitosamente"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Tipo de notificación.
     * Determina el color, icono y prioridad en el frontend.
     *
     * @see NotificationType
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * Tipo de entidad relacionada con la notificación.
     * Ejemplos: "ORDER", "TRANSPORT", "DOCUMENT"
     * <p>
     * Usado para navegación en el frontend cuando el usuario hace click.
     * </p>
     */
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    /**
     * ID de la entidad relacionada.
     * Ejemplo: "123" (ID de la orden)
     * <p>
     * Combinado con relatedEntityType permite navegar a la entidad específica.
     * Ejemplo: /order/123
     * </p>
     */
    @Column(name = "related_entity_id")
    private String relatedEntityId;

    /**
     * ID del usuario destinatario de la notificación.
     * Referencia al campo id de la tabla users.
     * <p>
     * Usado para filtrar notificaciones por usuario y para enviar push notifications
     * usando external_user_id en OneSignal.
     * </p>
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Indica si la notificación ha sido leída por el usuario.
     * Default: false
     * <p>
     * Usado para mostrar indicadores visuales (punto azul) y contar notificaciones no leídas.
     * </p>
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * Fecha y hora en que la notificación fue marcada como leída.
     * Null si aún no ha sido leída.
     * <p>
     * Permite auditoría y análisis de engagement de usuarios.
     * </p>
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Datos adicionales en formato JSON.
     * Puede contener información contextual específica del evento.
     * <p>
     * Ejemplo para orden creada:
     * <pre>
     * {
     *   "orderId": "123",
     *   "orderStatus": "PENDING_APPROVAL",
     *   "clientName": "John Doe"
     * }
     * </pre>
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
}
