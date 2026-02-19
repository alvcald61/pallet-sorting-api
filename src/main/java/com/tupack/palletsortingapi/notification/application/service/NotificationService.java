package com.tupack.palletsortingapi.notification.application.service;

import com.tupack.palletsortingapi.common.dto.PageResponse;
import com.tupack.palletsortingapi.notification.application.dto.CreateNotificationDTO;
import com.tupack.palletsortingapi.notification.application.dto.NotificationDTO;
import com.tupack.palletsortingapi.notification.application.dto.UnreadCountDTO;
import com.tupack.palletsortingapi.notification.domain.Notification;
import com.tupack.palletsortingapi.notification.infrastructure.outbound.database.NotificationRepository;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la gestión de notificaciones del sistema.
 * <p>
 * Este servicio orquesta toda la lógica de negocio relacionada con notificaciones,
 * incluyendo creación, consulta, actualización y eliminación. Actúa como capa
 * de coordinación entre el dominio, la persistencia y servicios externos (OneSignal).
 * </p>
 *
 * <h2>Responsabilidades Principales</h2>
 * <ul>
 *   <li><strong>Persistencia dual:</strong> Guardar notificaciones en BD + enviar push via OneSignal</li>
 *   <li><strong>Consultas paginadas:</strong> Obtener notificaciones con filtros y ordenamiento</li>
 *   <li><strong>Gestión de estado:</strong> Marcar como leídas, eliminar, contar no leídas</li>
 *   <li><strong>Notificaciones masivas:</strong> Enviar a múltiples usuarios (ej: todos los ADMINs)</li>
 *   <li><strong>Control de autorización:</strong> Verificar que los usuarios solo accedan a sus notificaciones</li>
 * </ul>
 *
 * <h2>Flujo Típico de Notificación</h2>
 * <pre>
 * 1. Evento del sistema ocurre (ej: orden creada)
 * 2. Event listener llama a createNotification()
 * 3. NotificationService:
 *    a. Crea registro en tabla notifications
 *    b. Llama a OneSignalService.sendPushNotification()
 *    c. OneSignal envía push a todos los dispositivos del usuario
 * 4. Frontend recibe push (foreground o background)
 * 5. Usuario consulta historial con getUserNotifications()
 * </pre>
 *
 * <h2>Características de Seguridad</h2>
 * <p>
 * Todos los métodos que modifican o consultan notificaciones verifican que el
 * userId del solicitante coincida con el propietario de la notificación, evitando
 * accesos no autorizados.
 * </p>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see OneSignalService
 * @see NotificationRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    /**
     * Repositorio para persistencia de notificaciones.
     * Provee operaciones CRUD y queries personalizadas.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Repositorio de usuarios.
     * Usado para obtener listas de usuarios por rol (ej: todos los ADMINs).
     */
    private final UserRepository userRepository;

    /**
     * Servicio externo para envío de push notifications vía OneSignal.
     * Maneja la comunicación con la API de OneSignal.
     */
    private final OneSignalService oneSignalService;

    /**
     * Crea una nueva notificación y la envía al usuario vía push notification.
     * <p>
     * Este método implementa el patrón de <strong>persistencia dual</strong>:
     * <ol>
     *   <li>Guarda la notificación en la base de datos para historial persistente</li>
     *   <li>Envía push notification vía OneSignal para notificación inmediata</li>
     * </ol>
     * </p>
     *
     * <h3>Uso Típico desde Event Listeners:</h3>
     * <pre>
     * {@code
     * @EventListener
     * @Async
     * public void onOrderCreated(OrderCreatedEvent event) {
     *     CreateNotificationDTO dto = CreateNotificationDTO.builder()
     *         .title("Orden Creada")
     *         .message("Tu orden #" + event.getOrder().getId() + " fue creada")
     *         .type(NotificationType.ORDER_CREATED)
     *         .relatedEntityType("ORDER")
     *         .relatedEntityId(event.getOrder().getId())
     *         .userId(event.getOrder().getCreatedBy())
     *         .build();
     *
     *     notificationService.createNotification(dto);
     * }
     * }
     * </pre>
     *
     * <h3>Datos Enviados a OneSignal:</h3>
     * <p>
     * El payload de push incluye:
     * <ul>
     *   <li><strong>title:</strong> Título de la notificación</li>
     *   <li><strong>message:</strong> Contenido descriptivo</li>
     *   <li><strong>data.entityType:</strong> Tipo de entidad relacionada (ej: "ORDER")</li>
     *   <li><strong>data.entityId:</strong> ID de la entidad (ej: "123")</li>
     * </ul>
     * Estos datos permiten al frontend navegar a la entidad cuando el usuario hace click.
     * </p>
     *
     * @param dto Datos de la notificación a crear. Todos los campos obligatorios
     *            (title, message, type, userId) deben estar presentes.
     * @return NotificationDTO con los datos de la notificación creada, incluyendo
     *         el ID generado por la base de datos.
     *
     * @throws org.springframework.dao.DataIntegrityViolationException si el userId no existe
     *         (violación de foreign key constraint)
     *
     * @see OneSignalService#sendPushNotification(String, String, String, Map)
     */
    @Transactional
    public NotificationDTO createNotification(CreateNotificationDTO dto) {
        Notification notification =
            Notification.builder().title(dto.getTitle()).message(dto.getMessage())
                .type(dto.getType()).relatedEntityType(dto.getRelatedEntityType())
                .relatedEntityId(dto.getRelatedEntityId()).userId(dto.getUserId().replace(
                    "tupack_", "")).isRead(false)
                .metadata(dto.getMetadata()).build();

        Notification saved = notificationRepository.save(notification);

        // Send push notification
        Map<String, Object> pushData = new HashMap<>();
        if (dto.getRelatedEntityType() != null) {
            pushData.put("entityType", dto.getRelatedEntityType());
        }
        if (dto.getRelatedEntityId() != null) {
            pushData.put("entityId", dto.getRelatedEntityId());
        }
        oneSignalService.sendPushNotification(dto.getUserId(), dto.getTitle(), dto.getMessage(),
            pushData);

        log.info("Notification created for user: {}", dto.getUserId());
        return toDTO(saved);
    }

    /**
     * Crea y envía la misma notificación a múltiples usuarios simultáneamente.
     * <p>
     * Método optimizado para notificaciones broadcast o grupales. Genera una copia
     * de la notificación para cada usuario y las persiste en batch, luego envía
     * una única petición de push notification a OneSignal con múltiples destinatarios.
     * </p>
     *
     * <h3>Casos de Uso Típicos:</h3>
     * <ul>
     *   <li><strong>Notificar a todos los ADMINs:</strong> Nueva orden pendiente de aprobación</li>
     *   <li><strong>Broadcast del sistema:</strong> Mantenimiento programado, nueva feature</li>
     *   <li><strong>Alertas por departamento:</strong> Notificar a todos los drivers de una zona</li>
     * </ul>
     *
     * <h3>Ejemplo de Implementación:</h3>
     * <pre>
     * {@code
     * @EventListener
     * @Async
     * public void onOrderCreated(OrderCreatedEvent event) {
     *     // Notificar a todos los usuarios con rol ADMIN
     *     List<String> adminIds = notificationService.getUserIdsByRole("ADMIN");
     *
     *     CreateNotificationDTO dto = CreateNotificationDTO.builder()
     *         .title("Nueva Orden Pendiente")
     *         .message("Orden #" + event.getOrder().getId() + " requiere aprobación")
     *         .type(NotificationType.ORDER_CREATED)
     *         .relatedEntityType("ORDER")
     *         .relatedEntityId(event.getOrder().getId())
     *         .build();
     *
     *     notificationService.createNotificationForMultipleUsers(adminIds, dto);
     * }
     * }
     * </pre>
     *
     * <h3>Optimización de Rendimiento:</h3>
     * <p>
     * En lugar de N llamadas HTTP a OneSignal (una por usuario), este método hace:
     * <ul>
     *   <li>1 batch insert a la base de datos (saveAll)</li>
     *   <li>1 petición HTTP a OneSignal con múltiples external_user_ids</li>
     * </ul>
     * Esto reduce significativamente la latencia para notificaciones a grupos grandes.
     * </p>
     *
     * @param userIds  Lista de IDs de usuarios destinatarios. No debe estar vacía.
     *                 Cada ID debe corresponder a un usuario válido en la BD.
     * @param baseDto  Template de notificación que se replicará para cada usuario.
     *                 El campo userId del DTO es ignorado y se reemplaza por cada
     *                 elemento de userIds.
     *
     * @throws org.springframework.dao.DataIntegrityViolationException si algún userId no existe
     *
     * @see OneSignalService#sendPushNotificationToMultiple(List, String, String, Map)
     * @see #getUserIdsByRole(String)
     */
    @Transactional
    public void createNotificationForMultipleUsers(List<String> userIds,
        CreateNotificationDTO baseDto) {
        List<Notification> notifications = userIds.stream().map(
            userId -> Notification.builder().title(baseDto.getTitle()).message(baseDto.getMessage())
                .type(baseDto.getType()).relatedEntityType(baseDto.getRelatedEntityType())
                .relatedEntityId(baseDto.getRelatedEntityId()).userId(userId).isRead(false)
                .metadata(baseDto.getMetadata()).build()).collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        // Send push notifications
        Map<String, Object> pushData = new HashMap<>();
        if (baseDto.getRelatedEntityType() != null) {
            pushData.put("entityType", baseDto.getRelatedEntityType());
        }
        if (baseDto.getRelatedEntityId() != null) {
            pushData.put("entityId", baseDto.getRelatedEntityId());
        }
        oneSignalService.sendPushNotificationToMultiple(userIds, baseDto.getTitle(),
            baseDto.getMessage(), pushData);

        log.info("Notifications created for {} users", userIds.size());
    }

    /**
     * Obtiene las notificaciones de un usuario en formato paginado.
     * <p>
     * Este método es utilizado por el frontend para mostrar el historial de
     * notificaciones en el menú dropdown y en la página dedicada de notificaciones.
     * </p>
     *
     * <h3>Ordenamiento y Filtrado:</h3>
     * <ul>
     *   <li><strong>Orden:</strong> Siempre descendente por fecha de creación (más recientes primero)</li>
     *   <li><strong>Filtro opcional:</strong> Si unreadOnly=true, solo retorna notificaciones no leídas</li>
     *   <li><strong>Paginación:</strong> Soporta navegación estándar con página y tamaño</li>
     * </ul>
     *
     * <h3>Uso desde React Query (Frontend):</h3>
     * <pre>
     * {@code
     * const { data } = useQuery({
     *   queryKey: ["notifications", page, size, unreadOnly],
     *   queryFn: () => getNotifications(page, size, unreadOnly),
     *   staleTime: 30 * 1000,
     *   refetchInterval: 30 * 1000, // Polling cada 30s
     * });
     * }
     * </pre>
     *
     * <h3>Estructura de Respuesta:</h3>
     * <pre>
     * {
     *   "data": [
     *     {
     *       "id": 1,
     *       "title": "Orden Creada",
     *       "message": "Tu orden #123...",
     *       "type": "ORDER_CREATED",
     *       "relatedEntityType": "ORDER",
     *       "relatedEntityId": "123",
     *       "isRead": false,
     *       "readAt": null,
     *       "createdAt": "2026-02-17T10:30:00"
     *     }
     *   ],
     *   "totalElements": 45,
     *   "totalPages": 3,
     *   "currentPage": 0,
     *   "pageSize": 20
     * }
     * </pre>
     *
     * @param userId      ID del usuario propietario de las notificaciones.
     * @param page        Número de página (base 0). Ejemplo: 0 para la primera página.
     * @param size        Cantidad de elementos por página. Recomendado: 20 para UI, 50 para batch.
     * @param unreadOnly  Si es true, filtra solo notificaciones no leídas (isRead=false).
     *                    Si es false, retorna todas las notificaciones del usuario.
     *
     * @return PageResponse con la lista de notificaciones y metadatos de paginación.
     *         Si no hay notificaciones, retorna data vacío pero nunca null.
     *
     * @see NotificationRepository#findByUserIdWithFilter(String, boolean, Pageable)
     */
    public PageResponse<NotificationDTO> getUserNotifications(String userId, int page, int size,
        boolean unreadOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage =
            notificationRepository.findByUserIdWithFilter(userId, unreadOnly, pageable);

        List<NotificationDTO> notifications =
            notificationPage.getContent().stream().map(this::toDTO).collect(Collectors.toList());

        return PageResponse.<NotificationDTO>builder().data(notifications)
            .totalElements(notificationPage.getTotalElements())
            .totalPages(notificationPage.getTotalPages()).currentPage(notificationPage.getNumber())
            .pageSize(notificationPage.getSize()).build();
    }

    /**
     * Obtiene el contador de notificaciones no leídas de un usuario.
     * <p>
     * Este método es crítico para la UX, ya que alimenta el badge numérico
     * que se muestra en el icono de notificaciones del navbar.
     * </p>
     *
     * <h3>Optimización de Rendimiento:</h3>
     * <p>
     * Utiliza COUNT(*) optimizado en lugar de cargar todas las notificaciones.
     * La query aprovecha el índice compuesto (user_id, is_read, created_at) para
     * responder en tiempo constante O(1), incluso con millones de notificaciones.
     * </p>
     *
     * <h3>Integración con Frontend (Polling):</h3>
     * <pre>
     * {@code
     * const { data } = useQuery({
     *   queryKey: ["notifications", "unread-count"],
     *   queryFn: async () => {
     *     const result = await getUnreadCount();
     *     setUnreadCount(result.data.count); // Sync con Zustand
     *     return result;
     *   },
     *   staleTime: 10 * 1000,
     *   refetchInterval: 10 * 1000, // Polling cada 10s
     * });
     * }
     * </pre>
     *
     * <h3>Ejemplo de Respuesta:</h3>
     * <pre>
     * {
     *   "count": 3
     * }
     * </pre>
     *
     * @param userId ID del usuario para contar sus notificaciones no leídas.
     *
     * @return UnreadCountDTO con el número de notificaciones donde isRead=false.
     *         Siempre retorna un valor (mínimo 0), nunca null.
     *
     * @see NotificationRepository#countUnreadByUserId(String)
     */
    public UnreadCountDTO getUnreadCount(String userId) {
        long count = notificationRepository.countUnreadByUserId(userId);
        return UnreadCountDTO.builder().count(count).build();
    }

    /**
     * Marca una notificación como leída y registra la fecha/hora de lectura.
     * <p>
     * Este método actualiza dos campos:
     * <ul>
     *   <li><strong>isRead:</strong> Cambia de false a true</li>
     *   <li><strong>readAt:</strong> Registra timestamp actual</li>
     * </ul>
     * </p>
     *
     * <h3>Control de Autorización:</h3>
     * <p>
     * Verifica que el userId solicitante sea el propietario de la notificación.
     * Si no coinciden, lanza RuntimeException("Unauthorized").
     * Esto previene que usuarios marquen notificaciones de otros.
     * </p>
     *
     * <h3>Uso desde Frontend:</h3>
     * <pre>
     * {@code
     * const markAsReadMutation = useMutation({
     *   mutationFn: markAsRead,
     *   onMutate: async (id) => {
     *     // Optimistic update
     *     queryClient.setQueryData(["notifications"], (old) => ({
     *       ...old,
     *       data: old.data.map(n => n.id === id ? {...n, isRead: true} : n)
     *     }));
     *   },
     *   onSettled: () => {
     *     queryClient.invalidateQueries(["notifications"]);
     *     queryClient.invalidateQueries(["notifications", "unread-count"]);
     *   },
     * });
     *
     * // Marcar al hacer click en notificación
     * const handleNotificationClick = (notification) => {
     *   markAsReadMutation.mutate(notification.id);
     *   router.push(`/${notification.relatedEntityType.toLowerCase()}/${notification.relatedEntityId}`);
     * };
     * }
     * </pre>
     *
     * <h3>Efectos Secundarios:</h3>
     * <ul>
     *   <li>Decrementa el contador de no leídas (getUnreadCount)</li>
     *   <li>El badge en el navbar se actualiza automáticamente (polling)</li>
     *   <li>El indicador visual (punto azul) desaparece en el UI</li>
     * </ul>
     *
     * @param id     ID de la notificación a marcar como leída.
     * @param userId ID del usuario solicitante. Debe ser el propietario de la notificación.
     *
     * @return NotificationDTO actualizado con isRead=true y readAt poblado.
     *
     * @throws RuntimeException con mensaje "Notification not found" si el ID no existe.
     * @throws RuntimeException con mensaje "Unauthorized" si userId no es el propietario.
     *
     * @see #getUnreadCount(String)
     */
    @Transactional
    public NotificationDTO markAsRead(Long id, String userId) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification updated = notificationRepository.save(notification);

        log.info("Notification {} marked as read", id);
        return toDTO(updated);
    }

    /**
     * Marca todas las notificaciones no leídas de un usuario como leídas en batch.
     * <p>
     * Método de conveniencia para usuarios que quieren limpiar todas sus
     * notificaciones pendientes de una sola vez. Común en aplicaciones que
     * tienen un botón "Marcar todas como leídas" en el menú de notificaciones.
     * </p>
     *
     * <h3>Implementación:</h3>
     * <p>
     * Obtiene todas las notificaciones no leídas del usuario (unreadOnly=true),
     * actualiza cada una en memoria (isRead=true, readAt=now), y persiste
     * todas en un solo batch (saveAll).
     * </p>
     *
     * <h3>Consideraciones de Rendimiento:</h3>
     * <p>
     * <strong>NOTA:</strong> Actualmente carga TODAS las notificaciones no leídas
     * en memoria (PageRequest con MAX_VALUE). Para usuarios con miles de notificaciones
     * no leídas, esto podría causar issues de memoria.
     * </p>
     * <p>
     * <strong>Mejora futura recomendada:</strong> Usar query nativa SQL con UPDATE masivo:
     * <pre>
     * {@code
     * UPDATE notifications
     * SET is_read = true, read_at = NOW()
     * WHERE user_id = ? AND is_read = false
     * }
     * </pre>
     * Esto evitaría cargar datos en memoria y sería más eficiente.
     * </p>
     *
     * <h3>Uso desde Frontend:</h3>
     * <pre>
     * {@code
     * const markAllAsReadMutation = useMutation({
     *   mutationFn: markAllAsRead,
     *   onSuccess: () => {
     *     queryClient.invalidateQueries(["notifications"]);
     *     queryClient.invalidateQueries(["notifications", "unread-count"]);
     *     notifications.show({
     *       title: "Éxito",
     *       message: "Todas las notificaciones marcadas como leídas",
     *       color: "green",
     *     });
     *   },
     * });
     *
     * // Botón en NotificationMenu
     * <Button onClick={() => markAllAsReadMutation.mutate()}>
     *   Marcar todas como leídas
     * </Button>
     * }
     * </pre>
     *
     * <h3>Efectos Secundarios:</h3>
     * <ul>
     *   <li>Contador de no leídas (getUnreadCount) vuelve a 0</li>
     *   <li>Badge en navbar desaparece</li>
     *   <li>Todas las notificaciones pierden el indicador visual de "nueva"</li>
     * </ul>
     *
     * @param userId ID del usuario propietario de las notificaciones a actualizar.
     *
     * @see #markAsRead(Long, String)
     * @see #getUnreadCount(String)
     */
    @Transactional
    public void markAllAsRead(String userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Notification> notifications =
            notificationRepository.findByUserIdWithFilter(userId, true, pageable);

        notifications.getContent().forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(notifications.getContent());
        log.info("All notifications marked as read for user: {}", userId);
    }

    /**
     * Elimina permanentemente una notificación del historial del usuario.
     * <p>
     * Operación destructiva que elimina el registro de la base de datos.
     * No se puede deshacer (no hay soft delete).
     * </p>
     *
     * <h3>Control de Autorización:</h3>
     * <p>
     * Verifica que el userId solicitante sea el propietario de la notificación.
     * Si no coinciden, lanza RuntimeException("Unauthorized").
     * Esto previene que usuarios eliminen notificaciones de otros.
     * </p>
     *
     * <h3>Uso desde Frontend:</h3>
     * <pre>
     * {@code
     * const deleteNotificationMutation = useMutation({
     *   mutationFn: deleteNotification,
     *   onSuccess: () => {
     *     queryClient.invalidateQueries(["notifications"]);
     *     queryClient.invalidateQueries(["notifications", "unread-count"]);
     *   },
     * });
     *
     * // Botón X en NotificationItem
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
     *   <li>Si la notificación era no leída, decrementa el contador (getUnreadCount)</li>
     *   <li>La notificación desaparece del historial y no se puede recuperar</li>
     *   <li>No afecta push notifications ya enviadas al dispositivo</li>
     * </ul>
     *
     * <h3>Comportamiento CASCADE:</h3>
     * <p>
     * No hay cascadas en este caso ya que Notification no tiene relaciones hijas.
     * Es una tabla de hojas en el modelo de datos.
     * </p>
     *
     * @param id     ID de la notificación a eliminar.
     * @param userId ID del usuario solicitante. Debe ser el propietario de la notificación.
     *
     * @throws RuntimeException con mensaje "Notification not found" si el ID no existe.
     * @throws RuntimeException con mensaje "Unauthorized" si userId no es el propietario.
     *
     * @see #clearAllNotifications(String)
     */
    @Transactional
    public void deleteNotification(Long id, String userId) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted", id);
    }

    /**
     * Elimina permanentemente todas las notificaciones de un usuario.
     * <p>
     * Operación destructiva que limpia completamente el historial de notificaciones.
     * Útil para usuarios que quieren hacer "reset" de su buzón de notificaciones.
     * </p>
     *
     * <h3>Implementación:</h3>
     * <p>
     * Usa query personalizada en repositorio que ejecuta:
     * <pre>
     * {@code
     * DELETE FROM notifications WHERE user_id = ?
     * }
     * </pre>
     * Eliminación en batch optimizada, no carga datos en memoria.
     * </p>
     *
     * <h3>Uso desde Frontend:</h3>
     * <pre>
     * {@code
     * const clearAllMutation = useMutation({
     *   mutationFn: clearAllNotifications,
     *   onMutate: async () => {
     *     // Confirmación de usuario antes de ejecutar
     *     const confirmed = window.confirm(
     *       "¿Estás seguro de eliminar todas las notificaciones? Esta acción no se puede deshacer."
     *     );
     *     if (!confirmed) throw new Error("Cancelled");
     *   },
     *   onSuccess: () => {
     *     queryClient.setQueryData(["notifications"], { data: [] });
     *     queryClient.setQueryData(["notifications", "unread-count"], { count: 0 });
     *     notifications.show({
     *       title: "Historial limpio",
     *       message: "Todas las notificaciones fueron eliminadas",
     *       color: "blue",
     *     });
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
     * <h3>Consideraciones de Seguridad:</h3>
     * <p>
     * No hay verificación adicional de autorización porque el método solo
     * elimina las notificaciones del userId proporcionado. El controlador
     * debe asegurar que el userId venga del token JWT del usuario autenticado.
     * </p>
     *
     * @param userId ID del usuario cuyas notificaciones serán eliminadas.
     *               TODAS las notificaciones de este usuario (leídas y no leídas)
     *               se eliminarán permanentemente.
     *
     * @see NotificationRepository#deleteAllByUserId(String)
     * @see #deleteNotification(Long, String)
     */
    @Transactional
    public void clearAllNotifications(String userId) {
        notificationRepository.deleteAllByUserId(userId);
        log.info("All notifications cleared for user: {}", userId);
    }

    /**
     * Obtiene los IDs de todos los usuarios que tienen un rol específico.
     * <p>
     * Método auxiliar utilizado principalmente para notificaciones grupales
     * basadas en roles. Permite notificar a todos los miembros de un rol
     * sin tener que conocer sus IDs individuales.
     * </p>
     *
     * <h3>Casos de Uso Típicos:</h3>
     * <ul>
     *   <li><strong>ADMIN:</strong> Notificar a todos los administradores de nuevas órdenes pendientes</li>
     *   <li><strong>DRIVER:</strong> Broadcast de cambios en políticas de transporte</li>
     *   <li><strong>CLIENT:</strong> Notificaciones de mantenimiento o nuevas features</li>
     * </ul>
     *
     * <h3>Ejemplo de Uso en Event Listener:</h3>
     * <pre>
     * {@code
     * @EventListener
     * @Async
     * public void onOrderCreated(OrderCreatedEvent event) {
     *     // Obtener todos los ADMINs
     *     List<String> adminIds = notificationService.getUserIdsByRole("ADMIN");
     *
     *     if (!adminIds.isEmpty()) {
     *         CreateNotificationDTO dto = CreateNotificationDTO.builder()
     *             .title("Nueva Orden Pendiente")
     *             .message("Orden #" + event.getOrder().getId() + " requiere aprobación")
     *             .type(NotificationType.ORDER_CREATED)
     *             .build();
     *
     *         notificationService.createNotificationForMultipleUsers(adminIds, dto);
     *     }
     * }
     * }
     * </pre>
     *
     * <h3>Implementación de la Query:</h3>
     * <p>
     * Usa relación many-to-many entre User y Role:
     * <pre>
     * {@code
     * SELECT u.id FROM users u
     * JOIN user_roles ur ON u.id = ur.user_id
     * JOIN roles r ON ur.role_id = r.id
     * WHERE r.name = ?
     * }
     * </pre>
     * </p>
     *
     * <h3>Consideraciones:</h3>
     * <ul>
     *   <li>Si el rol no existe o no tiene usuarios, retorna lista vacía (no null)</li>
     *   <li>Usuarios inactivos (enabled=false) también se incluyen</li>
     *   <li>No tiene paginación - carga todos los IDs en memoria</li>
     *   <li>Para roles con miles de usuarios, considerar paginación en futuro</li>
     * </ul>
     *
     * @param roleName Nombre exacto del rol (case-sensitive). Ejemplos: "ADMIN", "DRIVER", "CLIENT"
     *
     * @return Lista de IDs de usuarios (Strings) que tienen el rol especificado.
     *         Lista vacía si no hay usuarios con ese rol. Nunca retorna null.
     *
     * @see UserRepository#findByRoles_Name(String)
     * @see #createNotificationForMultipleUsers(List, CreateNotificationDTO)
     */
    public List<String> getUserIdsByRole(String roleName) {
        List<User> users = userRepository.findByRoles_Name(roleName);
        return users.stream().map(user -> String.valueOf(user.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Notification a su representación DTO para la capa de presentación.
     * <p>
     * Método privado de utilidad que mapea campos de la entidad JPA al DTO
     * que se envía en las respuestas de la API REST. Oculta campos internos
     * de auditoría (updatedBy, enabled, etc.) que no son relevantes para el frontend.
     * </p>
     *
     * <h3>Campos Mapeados:</h3>
     * <ul>
     *   <li>id, title, message, type - Datos básicos</li>
     *   <li>relatedEntityType, relatedEntityId - Para navegación</li>
     *   <li>isRead, readAt - Estado de lectura</li>
     *   <li>metadata - Datos adicionales en formato JSON</li>
     *   <li>createdAt - Timestamp de creación</li>
     * </ul>
     *
     * <h3>Campos NO Incluidos:</h3>
     * <ul>
     *   <li>userId - Ya está implícito en el contexto del usuario autenticado</li>
     *   <li>updatedAt, createdBy, updatedBy, enabled - Auditoría interna</li>
     * </ul>
     *
     * @param notification Entidad JPA de dominio a convertir. No debe ser null.
     *
     * @return NotificationDTO con todos los campos públicos poblados.
     *         Nunca retorna null si la entrada es válida.
     */
    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder().id(notification.getId()).title(notification.getTitle())
            .message(notification.getMessage()).type(notification.getType())
            .relatedEntityType(notification.getRelatedEntityType())
            .relatedEntityId(notification.getRelatedEntityId()).isRead(notification.getIsRead())
            .readAt(notification.getReadAt()).metadata(notification.getMetadata())
            .createdAt(notification.getCreatedAt()).build();
    }
}
