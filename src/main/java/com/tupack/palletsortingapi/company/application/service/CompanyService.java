package com.tupack.palletsortingapi.company.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.company.application.dto.CompanyCreateDto;
import com.tupack.palletsortingapi.company.application.dto.CompanyDto;
import com.tupack.palletsortingapi.company.domain.Company;
import com.tupack.palletsortingapi.company.domain.exception.CompanyNotFoundException;
import com.tupack.palletsortingapi.company.infrastructure.outbound.database.CompanyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Cacheable("companies")
    public List<CompanyDto> getAll() {
        return companyRepository.findAllByEnabledTrue().stream()
            .map(this::toDto)
            .toList();
    }

    @Cacheable(value = "company", key = "#id")
    public CompanyDto getById(Long id) {
        return companyRepository.findById(id)
            .filter(Company::isEnabled)
            .map(this::toDto)
            .orElseThrow(() -> new CompanyNotFoundException(id));
    }

    @CacheEvict(value = "companies", allEntries = true)
    public CompanyDto create(CompanyCreateDto dto) {
        if (companyRepository.existsByRuc(dto.getRuc())) {
            throw new BusinessException("Ya existe una empresa con el RUC " + dto.getRuc(), "DUPLICATE_RUC");
        }
        Company company = Company.builder()
            .name(dto.getName())
            .ruc(dto.getRuc())
            .build();
        return toDto(companyRepository.save(company));
    }

    @Caching(evict = {
        @CacheEvict(value = "companies", allEntries = true),
        @CacheEvict(value = "company", key = "#id"),
    })
    public CompanyDto update(Long id, CompanyCreateDto dto) {
        Company company = companyRepository.findById(id)
            .filter(Company::isEnabled)
            .orElseThrow(() -> new CompanyNotFoundException(id));
        company.setName(dto.getName());
        company.setRuc(dto.getRuc());
        return toDto(companyRepository.save(company));
    }

    @Caching(evict = {
        @CacheEvict(value = "companies", allEntries = true),
        @CacheEvict(value = "company", key = "#id"),
    })
    public void delete(Long id) {
        Company company = companyRepository.findById(id)
            .filter(Company::isEnabled)
            .orElseThrow(() -> new CompanyNotFoundException(id));
        company.setEnabled(false);
        companyRepository.save(company);
    }

    private CompanyDto toDto(Company company) {
        return CompanyDto.builder()
            .id(company.getId())
            .name(company.getName())
            .ruc(company.getRuc())
            .build();
    }
}
