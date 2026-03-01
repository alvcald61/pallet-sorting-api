package com.tupack.palletsortingapi.notification.domain.event;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.TransportStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de dominio que se publica cuando el estado de transporte de una orden cambia
 * a través del endpoint /api/order/{orderId}/transport/status/quick.
 */
@Getter
public class TransportStatusUpdatedEvent extends ApplicationEvent {

    private final Order order;
    private final TransportStatus newTransportStatus;

    public TransportStatusUpdatedEvent(Object source, Order order, TransportStatus newTransportStatus) {
        super(source);
        this.order = order;
        this.newTransportStatus = newTransportStatus;
    }
}
