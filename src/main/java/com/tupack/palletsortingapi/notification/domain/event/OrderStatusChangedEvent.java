package com.tupack.palletsortingapi.notification.domain.event;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de dominio que se publica cuando el estado de una orden cambia.
 * <p>
 * Este evento captura transiciones de estado en el ciclo de vida de una orden
 * (ej: PRE_APPROVED → APPROVED, APPROVED → IN_PROGRESS, etc.) y permite que
 * listeners reaccionen generando notificaciones, actualizando métricas, o
 * ejecutando lógica de negocio adicional.
 * </p>
 *
 * <h2>Flujo del Evento</h2>
 * <pre>
 * 1. Admin hace PATCH /api/order/123/status con body: { "status": "APPROVED" }
 * 2. OrderStatusService.updateOrderStatus() actualiza el estado
 * 3. OrderStatusService publica este evento:
 *    eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, oldStatus, newStatus))
 * 4. Spring propaga el evento a todos los @EventListener suscritos
 * 5. OrderEventListener.onOrderStatusChanged() recibe el evento (asíncrono)
 * 6. Se generan notificaciones contextuales según el nuevo estado
 * </pre>
 *
 * <h2>Información de Contexto</h2>
 * <p>
 * Este evento provee tres piezas de información clave:
 * <ul>
 *   <li><strong>order:</strong> Orden actualizada con el nuevo estado</li>
 *   <li><strong>oldStatus:</strong> Estado anterior (útil para auditoría y lógica condicional)</li>
 *   <li><strong>newStatus:</strong> Nuevo estado actual</li>
 * </ul>
 * Tener oldStatus y newStatus permite a los listeners implementar lógica
 * basada en transiciones específicas (ej: solo notificar si PRE_APPROVED → APPROVED).
 * </p>
 *
 * <h2>Listeners Suscritos</h2>
 * <ul>
 *   <li><strong>OrderEventListener.onOrderStatusChanged():</strong> Genera notificaciones contextuales</li>
 *   <li>Futuros listeners podrían: actualizar dashboards, ejecutar webhooks, etc.</li>
 * </ul>
 *
 * <h2>Transiciones de Estado Comunes</h2>
 * <table border="1">
 *   <tr>
 *     <th>De</th>
 *     <th>A</th>
 *     <th>Notificación Generada</th>
 *   </tr>
 *   <tr>
 *     <td>PRE_APPROVED</td>
 *     <td>APPROVED</td>
 *     <td>"Orden Aprobada" (Creador + Driver)</td>
 *   </tr>
 *   <tr>
 *     <td>PRE_APPROVED</td>
 *     <td>DENIED</td>
 *     <td>"Orden Denegada" (Creador)</td>
 *   </tr>
 *   <tr>
 *     <td>APPROVED</td>
 *     <td>IN_PROGRESS</td>
 *     <td>"Transporte Iniciado" (Creador)</td>
 *   </tr>
 *   <tr>
 *     <td>IN_PROGRESS</td>
 *     <td>DELIVERED</td>
 *     <td>"Orden Entregada" (Creador)</td>
 *   </tr>
 * </table>
 *
 * <h2>Ejemplo de Publicación</h2>
 * <pre>
 * {@code
 * @Service
 * public class OrderStatusService {
 *     private final ApplicationEventPublisher eventPublisher;
 *
 *     @Transactional
 *     public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
 *         Order order = orderRepository.findById(orderId).orElseThrow();
 *         OrderStatus oldStatus = order.getOrderStatus();
 *
 *         order.setOrderStatus(newStatus);
 *         Order updated = orderRepository.save(order);
 *
 *         eventPublisher.publishEvent(
 *             new OrderStatusChangedEvent(this, updated, oldStatus, newStatus)
 *         );
 *
 *         return updated;
 *     }
 * }
 * }
 * </pre>
 *
 * <h2>Ejemplo de Listener con Lógica Condicional</h2>
 * <pre>
 * {@code
 * @EventListener
 * @Async
 * public void onOrderStatusChanged(OrderStatusChangedEvent event) {
 *     Order order = event.getOrder();
 *     OrderStatus oldStatus = event.getOldStatus();
 *     OrderStatus newStatus = event.getNewStatus();
 *
 *     // Solo notificar al driver si la orden pasó de PRE_APPROVED a APPROVED
 *     if (oldStatus == OrderStatus.PRE_APPROVED && newStatus == OrderStatus.APPROVED) {
 *         notifyDriver(order);
 *     }
 * }
 * }
 * </pre>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see OrderEventListener#onOrderStatusChanged(OrderStatusChangedEvent)
 * @see OrderStatus
 * @see org.springframework.context.ApplicationEventPublisher
 */
@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {

    /**
     * La orden cuyo estado cambió.
     * <p>
     * Contiene el estado actualizado (newStatus) y todas las relaciones
     * cargadas (truck, driver, pallets) necesarias para generar notificaciones.
     * </p>
     */
    private final Order order;

    /**
     * Estado anterior de la orden antes del cambio.
     * <p>
     * Útil para auditoría y para implementar lógica condicional basada en
     * transiciones específicas (ej: PRE_APPROVED → APPROVED).
     * </p>
     */
    private final OrderStatus oldStatus;

    /**
     * Nuevo estado actual de la orden.
     * <p>
     * Determina qué tipo de notificación se genera y quiénes son los destinatarios.
     * Listeners pueden usar este campo para decisiones de negocio.
     * </p>
     */
    private final OrderStatus newStatus;

    /**
     * Constructor del evento de cambio de estado de orden.
     * <p>
     * Típicamente invocado por OrderStatusService después de actualizar
     * el estado en la base de datos.
     * </p>
     *
     * @param source    Objeto que publica el evento (usualmente OrderStatusService).
     *                  Spring usa esto para rastreo y debugging.
     * @param order     La orden con el estado actualizado. No debe ser null.
     * @param oldStatus Estado anterior de la orden. Puede ser null si es la primera
     *                  asignación de estado (raro en producción).
     * @param newStatus Nuevo estado actual de la orden. No debe ser null.
     */
    public OrderStatusChangedEvent(Object source, Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        super(source);
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
