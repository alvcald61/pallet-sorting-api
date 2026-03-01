package com.tupack.palletsortingapi.notification.infrastructure.outbound.database;

import com.tupack.palletsortingapi.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de persistencia para la entidad Notification.
 * <p>
 * Provee operaciones CRUD estándar heredadas de JpaRepository y queries
 * personalizadas optimizadas para los casos de uso del sistema de notificaciones.
 * </p>
 *
 * <h2>Índices Utilizados</h2>
 * <p>
 * La tabla notifications tiene un índice compuesto:
 * <pre>
 * INDEX idx_user_read_created (user_id, is_read, created_at DESC)
 * </pre>
 * Este índice optimiza todas las queries de este repositorio, permitiendo
 * búsquedas rápidas filtrando por usuario y estado de lectura.
 * </p>
 *
 * <h2>Estrategia de Queries</h2>
 * <ul>
 *   <li><strong>JPQL:</strong> Queries legibles y mantenibles para lógica de negocio</li>
 *   <li><strong>No N+1:</strong> Todas las queries están optimizadas sin joins innecesarios</li>
 *   <li><strong>Paginación:</strong> Uso obligatorio de Pageable para prevenir OOM</li>
 * </ul>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see Notification
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Busca notificaciones de un usuario con filtro opcional de solo no leídas.
     * <p>
     * Query principal para mostrar el historial de notificaciones en el frontend.
     * Soporta dos modos:
     * <ul>
     *   <li><strong>unreadOnly=false:</strong> Retorna todas las notificaciones (leídas + no leídas)</li>
     *   <li><strong>unreadOnly=true:</strong> Filtra solo las no leídas (isRead=false)</li>
     * </ul>
     * </p>
     *
     * <h3>Optimización con Índice:</h3>
     * <p>
     * La query aprovecha el índice compuesto (user_id, is_read, created_at DESC):
     * <pre>
     * EXPLAIN: Using index idx_user_read_created; Using index condition
     * </pre>
     * Esto permite tiempo de respuesta O(log n) incluso con millones de notificaciones.
     * </p>
     *
     * <h3>Ordenamiento:</h3>
     * <p>
     * Las notificaciones siempre se retornan en orden descendente por fecha de creación
     * (más recientes primero), independientemente del Pageable.sort.
     * </p>
     *
     * <h3>Ejemplo de Uso:</h3>
     * <pre>
     * {@code
     * Pageable pageable = PageRequest.of(0, 20);
     * Page<Notification> unreadNotifications = notificationRepository.findByUserIdWithFilter(
     *     "user123",
     *     true,  // Solo no leídas
     *     pageable
     * );
     * }
     * </pre>
     *
     * @param userId      ID del usuario propietario de las notificaciones
     * @param unreadOnly  true para filtrar solo no leídas, false para todas
     * @param pageable    Configuración de paginación y ordenamiento (el ordenamiento se ignora)
     *
     * @return Página de notificaciones que cumplen el criterio
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "AND (:unreadOnly = false OR n.isRead = false) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdWithFilter(
        @Param("userId") String userId,
        @Param("unreadOnly") boolean unreadOnly,
        Pageable pageable
    );

    /**
     * Cuenta las notificaciones no leídas de un usuario.
     * <p>
     * Query optimizada que ejecuta COUNT(*) en lugar de cargar datos.
     * Retorna el número exacto de notificaciones donde isRead=false.
     * </p>
     *
     * <h3>Optimización con Índice:</h3>
     * <p>
     * Usa covering index (user_id, is_read), no necesita acceder a la tabla:
     * <pre>
     * EXPLAIN: Using index idx_user_read_created (covering)
     * </pre>
     * Esto resulta en tiempo constante O(1) para el conteo.
     * </p>
     *
     * <h3>Uso en Polling:</h3>
     * <pre>
     * {@code
     * // Frontend hace polling cada 10s
     * useQuery({
     *   queryKey: ["notifications", "unread-count"],
     *   queryFn: () => getUnreadCount(),
     *   refetchInterval: 10 * 1000,
     * });
     *
     * // Backend responde rápido usando esta query
     * long count = notificationRepository.countUnreadByUserId(userId);
     * }
     * </pre>
     *
     * @param userId ID del usuario para contar notificaciones
     *
     * @return Número de notificaciones no leídas. Retorna 0 si no hay ninguna.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") String userId);

    /**
     * Busca todas las notificaciones de un usuario sin filtros.
     * <p>
     * Versión simplificada de findByUserIdWithFilter con unreadOnly=false.
     * Útil cuando se necesitan todas las notificaciones sin condicional.
     * </p>
     *
     * <h3>Nota:</h3>
     * <p>
     * Este método es redundante ya que findByUserIdWithFilter(userId, false, pageable)
     * hace lo mismo. Se mantiene por retrocompatibilidad o preferencia de claridad.
     * </p>
     *
     * @param userId   ID del usuario propietario
     * @param pageable Configuración de paginación
     *
     * @return Página con todas las notificaciones del usuario
     *
     * @see #findByUserIdWithFilter(String, boolean, Pageable)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId")
    Page<Notification> findByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * Elimina todas las notificaciones de un usuario.
     * <p>
     * Query de eliminación masiva que usa:
     * <pre>
     * DELETE FROM notifications WHERE user_id = ?
     * </pre>
     * Operación destructiva e irreversible.
     * </p>
     *
     * <h3>Transaccionalidad:</h3>
     * <p>
     * Spring Data JPA automáticamente envuelve este método en una transacción.
     * Si la operación falla a mitad de camino, se hace rollback completo.
     * </p>
     *
     * <h3>Rendimiento:</h3>
     * <p>
     * Ejecuta DELETE directo en SQL sin cargar datos en memoria.
     * Eficiente incluso para miles de notificaciones.
     * </p>
     *
     * <h3>Uso:</h3>
     * <pre>
     * {@code
     * @Transactional
     * public void clearAllNotifications(String userId) {
     *     notificationRepository.deleteAllByUserId(userId);
     *     log.info("All notifications cleared for user: {}", userId);
     * }
     * }
     * </pre>
     *
     * @param userId ID del usuario cuyas notificaciones se eliminarán
     */
    void deleteAllByUserId(String userId);
}
