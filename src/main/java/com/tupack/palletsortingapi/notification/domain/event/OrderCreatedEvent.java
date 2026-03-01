package com.tupack.palletsortingapi.notification.domain.event;

import com.tupack.palletsortingapi.order.domain.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de dominio que se publica cuando una nueva orden es creada.
 * <p>
 * Este evento forma parte de la arquitectura Event-Driven del sistema,
 * permitiendo que diferentes componentes reaccionen a la creación de órdenes
 * sin acoplamiento directo con el OrderService.
 * </p>
 *
 * <h2>Flujo del Evento</h2>
 * <pre>
 * 1. Cliente hace POST /api/order
 * 2. OrderPersistenceService.save() guarda la orden en BD
 * 3. OrderPersistenceService publica este evento:
 *    applicationEventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder))
 * 4. Spring propaga el evento a todos los @EventListener suscritos
 * 5. OrderEventListener.onOrderCreated() recibe el evento (asíncrono)
 * 6. Se generan notificaciones para el creador y los ADMINs
 * </pre>
 *
 * <h2>Listeners Suscritos</h2>
 * <ul>
 *   <li><strong>OrderEventListener.onOrderCreated():</strong> Genera notificaciones</li>
 *   <li>Futuros listeners podrían: enviar emails, actualizar analytics, etc.</li>
 * </ul>
 *
 * <h2>Patrón Event-Driven Benefits</h2>
 * <ul>
 *   <li><strong>Desacoplamiento:</strong> OrderService no conoce NotificationService</li>
 *   <li><strong>Extensibilidad:</strong> Nuevos listeners se agregan sin modificar OrderService</li>
 *   <li><strong>Asincronía:</strong> Listeners con @Async no bloquean la operación principal</li>
 *   <li><strong>Single Responsibility:</strong> Cada listener maneja una sola concern</li>
 * </ul>
 *
 * <h2>Ejemplo de Publicación</h2>
 * <pre>
 * {@code
 * @Service
 * public class OrderPersistenceService {
 *     private final ApplicationEventPublisher eventPublisher;
 *
 *     @Transactional
 *     public Order save(Order order) {
 *         Order savedOrder = orderRepository.save(order);
 *         eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));
 *         return savedOrder;
 *     }
 * }
 * }
 * </pre>
 *
 * <h2>Ejemplo de Listener</h2>
 * <pre>
 * {@code
 * @Component
 * public class OrderEventListener {
 *     @Async
 *     @EventListener
 *     public void onOrderCreated(OrderCreatedEvent event) {
 *         Order order = event.getOrder();
 *         // Lógica de notificaciones...
 *     }
 * }
 * }
 * </pre>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see OrderEventListener#onOrderCreated(OrderCreatedEvent)
 * @see org.springframework.context.ApplicationEventPublisher
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    /**
     * La orden que fue creada.
     * <p>
     * Contiene todos los datos de la orden recién persistida en la base de datos,
     * incluyendo su ID generado, relaciones cargadas (truck, pallets), y metadatos
     * de auditoría (createdBy, createdAt).
     * </p>
     */
    private final Order order;

    /**
     * Constructor del evento de orden creada.
     * <p>
     * Típicamente invocado por OrderPersistenceService inmediatamente después
     * de guardar la orden en la base de datos.
     * </p>
     *
     * @param source Objeto que publica el evento (usualmente OrderPersistenceService).
     *               Spring usa esto para rastreo y debugging.
     * @param order  La orden que fue creada. No debe ser null.
     */
    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
