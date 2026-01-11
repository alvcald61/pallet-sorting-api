package com.tupack.palletsortingapi.order.infrastructure.outbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersByClientDTO {
    private String id;
    private String clientName;
    private String businessName;
    private Long count;
}

