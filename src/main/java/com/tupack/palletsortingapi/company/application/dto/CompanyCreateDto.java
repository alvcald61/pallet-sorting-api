package com.tupack.palletsortingapi.company.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyCreateDto {

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 20)
    private String ruc;
}
