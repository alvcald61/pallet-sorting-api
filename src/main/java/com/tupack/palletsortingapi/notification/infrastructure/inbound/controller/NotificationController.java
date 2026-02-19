package com.tupack.palletsortingapi.notification.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.dto.PageResponse;
import com.tupack.palletsortingapi.notification.application.dto.NotificationDTO;
import com.tupack.palletsortingapi.notification.application.dto.UnreadCountDTO;
import com.tupack.palletsortingapi.notification.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de notificaciones del usuario.
 * <p>
 * Expone endpoints públicos para que usuarios autenticados puedan consultar,
 * marcar como leídas y eliminar sus notificaciones. Todos los endpoints
 * utilizan @AuthenticationPrincipal para obtener el usuario del contexto de
 * seguridad, asegurando que cada usuario solo acceda a sus propias notificaciones.
 * </p>
 *
 * <h2>Seguridad y Autorización</h2>
 * <p>
 * <strong>Autenticación requerida:</strong> Todos los endpoints requieren un token JWT válido.
 * El middleware de Spring Security valida el token y crea el UserDetails antes de
 * que la request llegue a este controlador.
 * </p>
 * <p>
 * <strong>Autorización implícita:</strong> No hay verificación explícita de roles
 * porque todos los usuarios autenticados tienen acceso a SUS notificaciones.
 * El userId se extrae del token JWT, no del request body, previniendo suplantación.
 * </p>
 *
 * <h2>Formato de Respuestas</h2>
 * <p>
 * Todas las respuestas siguen el formato estándar GenericResponse:
 * <pre>
 * {
 *   "success": true,
 *   "message": null,
 *   "data": { ... }
 * }
 * </pre>
 * </p>
 *
 * <h2>Endpoints Disponibles</h2>
 * <ul>
 *   <li>GET /api/notification - Lista paginada de notificaciones</li>
 *   <li>GET /api/notification/count/unread - Contador de no leídas</li>
 *   <li>PATCH /api/notification/{id}/read - Marcar una como leída</li>
 *   <li>PATCH /api/notification/read-all - Marcar todas como leídas</li>
 *   <li>DELETE /api/notification/{id} - Eliminar una notificación</li>
 *   <li>DELETE /api/notification/clear-all - Eliminar todas</li>
 * </ul>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see NotificationService
 * @see GenericResponse
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    /**
     * Servicio de aplicación que contiene la lógica de negocio de notificaciones.
     */
    private final NotificationService notificationService;

    /**
     * Obtiene la lista paginada de notificaciones del usuario autenticado.
     * <p>
     * Endpoint principal para mostrar el historial de notificaciones en el frontend.
     * Soporta filtrado opcional de solo no leídas y paginación estándar.
     * </p>
     *
     * <h3>Ejemplo de Request:</h3>
     * <pre>
     * GET /api/notification?page=0&size=20&unreadOnly=false
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <h3>Ejemplo de Response:</h3>
     * <pre>
     * {
     *   "success": true,
     *   "message": null,
     *   "data": {
     *     "data": [
     *       {
     *         "id": 1,
     *         "title": "Orden Creada",
     *         "message": "Tu orden #123 fue creada exitosamente",
     *         "type": "ORDER_CREATED",
     *         "relatedEntityType": "ORDER",
     *         "relatedEntityId": "123",
     *         "isRead": false,
     *         "readAt": null,
     *         "createdAt": "2026-02-17T10:30:00"
     *       }
     *     ],
     *     "totalElements": 45,
     *     "totalPages": 3,
     *     "currentPage": 0,
     *     "pageSize": 20
     *   }
     * }
     * </pre>
     *
     * @param userDetails Usuario autenticado inyectado por Spring Security.
     *                    Se extrae automáticamente del token JWT.
     * @param page        Número de página (base 0). Default: 0
     * @param size        Tamaño de página. Default: 20
     * @param unreadOnly  Filtrar solo no leídas. Default: false
     *
     * @return ResponseEntity con GenericResponse conteniendo PageResponse<NotificationDTO>
     */
    @GetMapping
    public ResponseEntity<GenericResponse> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        String userId = getUserId(userDetails);
        PageResponse<NotificationDTO> notifications = notificationService.getUserNotifications(userId, page, size
            , unreadOnly);
        return ResponseEntity.ok(GenericResponse.success(notifications));
    }

    /**
     * Obtiene el contador de notificaciones no leídas del usuario autenticado.
     * <p>
     * Endpoint crítico para la UX que alimenta el badge numérico en el navbar.
     * Diseñado para ser llamado frecuentemente mediante polling (cada 10 segundos).
     * </p>
     *
     * <h3>Optimización:</h3>
     * <p>
     * Utiliza COUNT(*) optimizado con índice covering. Responde en tiempo constante
     * O(1) incluso con millones de notificaciones.
     * </p>
     *
     * <h3>Ejemplo de Request:</h3>
     * <pre>
     * GET /api/notification/count/unread
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <h3>Ejemplo de Response:</h3>
     * <pre>
     * {
     *   "success": true,
     *   "message": null,
     *   "data": {
     *     "count": 3
     *   }
     * }
     * </pre>
     *
     * <h3>Uso con React Query (Frontend):</h3>
     * <pre>
     * {@code
     * useQuery({
     *   queryKey: ["notifications", "unread-count"],
     *   queryFn: getUnreadCount,
     *   staleTime: 10 * 1000,
     *   refetchInterval: 10 * 1000, // Poll cada 10s
     * });
     * }
     * </pre>
     *
     * @param userDetails Usuario autenticado inyectado por Spring Security
     *
     * @return ResponseEntity con GenericResponse conteniendo UnreadCountDTO
     */
    @GetMapping("/count/unread")
    public ResponseEntity<GenericResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        UnreadCountDTO count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(GenericResponse.success(count));
    }

    /**
     * Marca una notificación específica como leída.
     * <p>
     * Actualiza isRead=true y registra readAt con timestamp actual.
     * Endpoint típicamente llamado cuando el usuario hace click en una notificación.
     * </p>
     *
     * <h3>Validación de Autorización:</h3>
     * <p>
     * El servicio verifica que el userId del token coincida con el propietario
     * de la notificación. Si no coinciden, se lanza RuntimeException("Unauthorized").
     * </p>
     *
     * <h3>Ejemplo de Request:</h3>
     * <pre>
     * PATCH /api/notification/123/read
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <h3>Ejemplo de Response Exitosa:</h3>
     * <pre>
     * {
     *   "success": true,
     *   "message": null,
     *   "data": {
     *     "id": 123,
     *     "title": "Orden Creada",
     *     "message": "Tu orden #456...",
     *     "isRead": true,
     *     "readAt": "2026-02-17T15:45:30",
     *     "createdAt": "2026-02-17T10:30:00"
     *   }
     * }
     * </pre>
     *
     * <h3>Ejemplo de Error (Unauthorized):</h3>
     * <pre>
     * {
     *   "success": false,
     *   "message": "Unauthorized",
     *   "data": null
     * }
     * </pre>
     *
     * <h3>Uso con Optimistic Updates (Frontend):</h3>
     * <pre>
     * {@code
     * const mutation = useMutation({
     *   mutationFn: markAsRead,
     *   onMutate: async (id) => {
     *     // Update UI instantáneamente
     *     queryClient.setQueryData(["notifications"], (old) => ({
     *       ...old,
     *       data: old.data.map(n => n.id === id ? {...n, isRead: true} : n)
     *     }));
     *   },
     *   onError: (_, __, context) => {
     *     // Rollback si falla
     *     queryClient.setQueryData(["notifications"], context.previousData);
     *   },
     * });
     * }
     * </pre>
     *
     * @param id          ID de la notificación a marcar como leída
     * @param userDetails Usuario autenticado inyectado por Spring Security
     *
     * @return ResponseEntity con GenericResponse conteniendo la NotificationDTO actualizada
     *
     * @throws RuntimeException si la notificación no existe o el usuario no es el propietario
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<GenericResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        NotificationDTO notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(GenericResponse.success(notification));
    }

    /**
     * Marca todas las notificaciones no leídas del usuario como leídas.
     * <p>
     * Operación batch para usuarios que quieren limpiar todas sus notificaciones
     * pendientes de una sola vez. Común en UIs con botón "Marcar todas como leídas".
     * </p>
     *
     * <h3>Comportamiento:</h3>
     * <ul>
     *   <li>Solo afecta notificaciones donde isRead=false</li>
     *   <li>Todas se actualizan con isRead=true y readAt=now</li>
     *   <li>El contador de no leídas vuelve a 0</li>
     * </ul>
     *
     * <h3>Ejemplo de Request:</h3>
     * <pre>
     * PATCH /api/notification/read-all
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <h3>Ejemplo de Response:</h3>
     * <pre>
     * {
     *   "success": true,
     *   "message": null,
     *   "data": null
     * }
     * </pre>
     *
     * <h3>Efectos Secundarios:</h3>
     * <ul>
     *   <li>Badge en navbar desaparece (contador = 0)</li>
     *   <li>Todas las notificaciones pierden indicador visual de "nueva"</li>
     *   <li>Frontend debe invalidar queries de notificaciones y contador</li>
     * </ul>
     *
     * @param userDetails Usuario autenticado inyectado por Spring Security
     *
     * @return ResponseEntity con GenericResponse sin data (data=null)
     */
    @PatchMapping("/read-all")
    public ResponseEntity<GenericResponse> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(GenericResponse.success(null));
    }

    /**
     * Elimina permanentemente una notificación específica.
     * <p>
     * Operación destructiva que remueve el registro de la base de datos.
     * No se puede deshacer (no hay soft delete).
     * </p>
     *
     * <h3>Validación de Autorización:</h3>
     * <p>
     * El servicio verifica que el userId del token coincida con el propietario
     * de la notificación. Si no coinciden, se lanza RuntimeException("Unauthorized").
     * </p>
     *
     * <h3>Ejemplo de Request:</h3>
     * <pre>
     * DELETE /api/notification/123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <h3>Ejemplo de Response Exitosa:</h3>
     * <pre>
     * {
     *   "success": true,
     *   "message": null,
     *   "data": null
     * }
     * </pre>
     *
     * <h3>Uso Típico (Botón X en UI):</h3>
     * <pre>
     * {@code
     * <ActionIcon
     *   onClick={(e) => {
     *     e.stopPropagation();
     *     deleteNotificationMutation.mutate(notification.id);
     *   }}
     * >
     *   <IconX size={14} />
     * </ActionIcon>
     * }
     * </pre>
     *
     * <h3>Efectos Secundarios:</h3>
     * <ul>
     *   <li>Si era no leída, decrementa el contador</li>
     *   <li>Notificación desaparece del historial</li>
     *   <li>No afecta push notifications ya enviadas</li>
     * </ul>
     *
     * @param id          ID de la notificación a eliminar
     * @param userDetails Usuario autenticado inyectado por Spring Security
     *
     * @return ResponseEntity con GenericResponse sin data (data=null)
     *
     * @throws RuntimeException si la notificación no existe o el usuario no es el propietario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(GenericResponse.success(null));
    }

    /**
     * Elimina permanentemente todas las notificaciones del usuario.
     * <p>
     * Operación destructiva que limpia completamente el historial de notificaciones.
     * Tanto leídas como no leídas se eliminan. No se puede deshacer.
     * </p>
     *
     * <h3>Ejemplo de Request:</h3>
     * <pre>
     * DELETE /api/notification/clear-all
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <h3>Ejemplo de Response:</h3>
     * <pre>
     * {
     *   "success": true,
     *   "message": null,
     *   "data": null
     * }
     * </pre>
     *
     * <h3>Uso con Confirmación (Frontend):</h3>
     * <pre>
     * {@code
     * const clearAllMutation = useMutation({
     *   mutationFn: clearAllNotifications,
     *   onMutate: async () => {
     *     const confirmed = window.confirm(
     *       "¿Eliminar todas las notificaciones? No se puede deshacer."
     *     );
     *     if (!confirmed) throw new Error("Cancelled");
     *   },
     *   onSuccess: () => {
     *     queryClient.setQueryData(["notifications"], { data: [] });
     *     queryClient.setQueryData(["notifications", "unread-count"], { count: 0 });
     *   },
     * });
     * }
     * </pre>
     *
     * <h3>Efectos Secundarios:</h3>
     * <ul>
     *   <li>Contador de no leídas vuelve a 0</li>
     *   <li>Badge en navbar desaparece</li>
     *   <li>Menú de notificaciones muestra "No hay notificaciones"</li>
     *   <li>TODO el historial se pierde permanentemente</li>
     * </ul>
     *
     * <h3>Recomendación de UX:</h3>
     * <p>
     * Se recomienda mostrar un modal de confirmación antes de ejecutar esta acción,
     * ya que es irreversible y puede resultar en pérdida de información importante.
     * </p>
     *
     * @param userDetails Usuario autenticado inyectado por Spring Security
     *
     * @return ResponseEntity con GenericResponse sin data (data=null)
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<GenericResponse> clearAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        notificationService.clearAllNotifications(userId);
        return ResponseEntity.ok(GenericResponse.success(null));
    }

    /**
     * Extrae el ID del usuario desde el UserDetails proporcionado por Spring Security.
     * <p>
     * Método de utilidad que maneja la conversión del UserDetails genérico al
     * userId de tipo String usado en el sistema.
     * </p>
     *
     * <h3>Lógica de Extracción:</h3>
     * <ol>
     *   <li>Si userDetails es instancia de User (nuestra entidad), retorna user.getId()</li>
     *   <li>Si no, fallback a userDetails.getUsername()</li>
     * </ol>
     *
     * <h3>¿Por qué esta lógica?</h3>
     * <p>
     * Spring Security carga el UserDetails desde UserDetailsService. En nuestro caso,
     * cargamos la entidad User directamente, que implementa UserDetails. Esto permite
     * acceso directo al ID sin necesidad de queries adicionales.
     * </p>
     * <p>
     * El fallback a username es un safety net para tests o implementaciones
     * alternativas de UserDetailsService.
     * </p>
     *
     * <h3>Ejemplo de Flujo:</h3>
     * <pre>
     * Request Header: Authorization: Bearer eyJ...
     * → JWT Filter extrae token
     * → UserDetailsService carga User entity
     * → User instanceof UserDetails
     * → getUserId(userDetails) retorna user.getId()
     * </pre>
     *
     * @param userDetails UserDetails del usuario autenticado, inyectado por
     *                    {@literal @}AuthenticationPrincipal
     *
     * @return ID del usuario en formato String. Nunca null (Spring Security garantiza
     *         que userDetails no es null en endpoints autenticados).
     */
    private String getUserId(UserDetails userDetails) {
        if (userDetails instanceof com.tupack.palletsortingapi.user.domain.User) {
            return String.valueOf(((com.tupack.palletsortingapi.user.domain.User) userDetails).getId());
        }
        return userDetails.getUsername();
    }
}
