package com.tupack.palletsortingapi.notification.application.dto;

import com.tupack.palletsortingapi.notification.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object para la creación de notificaciones.
 * <p>
 * Contiene todos los datos necesarios para crear una nueva notificación
 * y enviarla al usuario correspondiente vía base de datos y push notification.
 * </p>
 * <p>
 * Este DTO es usado internamente por los listeners de eventos y servicios
 * para generar notificaciones automáticamente.
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
public class CreateNotificationDTO {

    /** Título de la notificación (requerido) */
    private String title;

    /** Mensaje descriptivo (requerido) */
    private String message;

    /** Tipo de notificación (requerido) */
    private NotificationType type;

    /** Tipo de entidad relacionada (opcional, ej: "ORDER") */
    private String relatedEntityType;

    /** ID de la entidad relacionada (opcional, ej: "123") */
    private String relatedEntityId;

    /**
     * ID del usuario destinatario (requerido).
     * Se usa como external_user_id en OneSignal.
     */
    private String userId;

    /**
     * Metadatos adicionales opcionales.
     * Útil para incluir información contextual del evento.
     */
    private Map<String, Object> metadata;
}
