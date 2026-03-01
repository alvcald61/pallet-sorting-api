package com.tupack.palletsortingapi.order.application.dto.dashboard;

import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersByStatusDTO {
    private String status;
    private String orderStatus;
    private Long count;
    private Long total;

    // Constructor for JPQL projection
    public OrdersByStatusDTO(OrderStatus orderStatus, Long count) {
        this.status = orderStatus.name();
        this.orderStatus = orderStatus.name();
        this.count = count;
        this.total = count;
    }
}

