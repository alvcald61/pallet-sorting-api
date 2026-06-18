package com.tupack.palletsortingapi.company.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyDto {
    private Long id;
    private String name;
    private String ruc;
}
