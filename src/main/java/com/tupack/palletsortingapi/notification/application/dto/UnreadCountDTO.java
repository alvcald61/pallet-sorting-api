package com.tupack.palletsortingapi.notification.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object para el contador de notificaciones no leídas.
 * <p>
 * Respuesta simple que contiene únicamente el número de notificaciones
 * sin leer de un usuario específico.
 * </p>
 * <p>
 * Utilizado por el frontend para mostrar el badge en el icono de notificaciones.
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
public class UnreadCountDTO {

    /**
     * Número de notificaciones no leídas del usuario.
     * Valor mínimo: 0
     */
    private long count;
}
