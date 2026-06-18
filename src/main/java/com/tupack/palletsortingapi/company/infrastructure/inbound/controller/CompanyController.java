package com.tupack.palletsortingapi.company.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.company.application.dto.CompanyCreateDto;
import com.tupack.palletsortingapi.company.application.service.CompanyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Gestión de empresas emisoras de facturas")
@PreAuthorize("hasAuthority('ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public GenericResponse getAll() {
        return GenericResponse.success(companyService.getAll());
    }

    @PostMapping
    public GenericResponse create(@RequestBody @Valid CompanyCreateDto dto) {
        return GenericResponse.created(companyService.create(dto));
    }

    @PutMapping("/{id}")
    public GenericResponse update(@PathVariable Long id, @RequestBody @Valid CompanyCreateDto dto) {
        return GenericResponse.success(companyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public GenericResponse delete(@PathVariable Long id) {
        companyService.delete(id);
        return GenericResponse.success("Empresa eliminada");
    }
}
