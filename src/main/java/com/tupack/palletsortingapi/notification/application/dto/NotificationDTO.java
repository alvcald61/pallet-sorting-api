package com.tupack.palletsortingapi.notification.application.dto;

import com.tupack.palletsortingapi.notification.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object para representar una notificación en las respuestas de la API.
 * <p>
 * Este DTO se utiliza para transferir información de notificaciones desde el backend
 * hacia el frontend, ocultando detalles de implementación de la entidad.
 * </p>
 * <p>
 * No incluye campos sensibles o de auditoría interna como updatedBy, enabled, etc.
 * </p>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    /** ID único de la notificación */
    private Long id;

    /** Título breve de la notificación */
    private String title;

    /** Mensaje descriptivo completo */
    private String message;

    /** Tipo de notificación que determina el estilo visual */
    private NotificationType type;

    /** Tipo de entidad relacionada (ORDER, TRANSPORT, etc.) */
    private String relatedEntityType;

    /** ID de la entidad relacionada para navegación */
    private String relatedEntityId;

    /** Indica si el usuario ya leyó la notificación */
    private Boolean isRead;

    /** Fecha y hora en que se marcó como leída (null si no leída) */
    private LocalDateTime readAt;

    /** Metadatos adicionales en formato clave-valor */
    private Map<String, Object> metadata;

    /** Fecha y hora de creación de la notificación */
    private LocalDateTime createdAt;
}
