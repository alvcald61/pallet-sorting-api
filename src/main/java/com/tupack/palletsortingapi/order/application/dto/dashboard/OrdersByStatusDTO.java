package com.tupack.palletsortingapi.order.application.dto.dashboard;

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
}

