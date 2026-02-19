package com.tupack.palletsortingapi.notification.domain.enums;

/**
 * Tipos de notificaciones disponibles en el sistema.
 * <p>
 * Cada tipo representa un evento específico que puede generar una notificación
 * para los usuarios del sistema TUPACK.
 * </p>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 */
public enum NotificationType {

    /**
     * Notificación enviada cuando se crea una nueva orden.
     * Destinatarios: Cliente creador y todos los ADMINs.
     */
    ORDER_CREATED,

    /**
     * Notificación genérica cuando cambia el estado de una orden.
     * Se usa cuando el cambio no tiene un tipo específico.
     */
    ORDER_STATUS_CHANGED,

    /**
     * Notificación enviada cuando una orden es aprobada por un administrador.
     * Destinatarios: Cliente creador y driver asignado.
     */
    ORDER_APPROVED,

    /**
     * Notificación enviada cuando una orden es denegada.
     * Destinatarios: Cliente creador.
     */
    ORDER_DENIED,

    /**
     * Notificación enviada cuando una orden requiere documentación pendiente.
     * Destinatarios: Cliente creador.
     */
    ORDER_DOCUMENT_PENDING,

    /**
     * Notificación enviada cuando el transporte de una orden es iniciado.
     * Destinatarios: Cliente creador.
     */
    TRANSPORT_STARTED,

    /**
     * Notificación enviada cuando el transporte es completado y entregado.
     * Destinatarios: Cliente creador.
     */
    TRANSPORT_DELIVERED,

    /**
     * Notificación genérica para alertas del sistema.
     * Puede ser enviada a cualquier usuario según el contexto.
     */
    SYSTEM_ALERT
}
