package com.tupack.palletsortingapi.company.domain.exception;

import com.tupack.palletsortingapi.common.exception.ResourceNotFoundException;

public class CompanyNotFoundException extends ResourceNotFoundException {

    public CompanyNotFoundException(Long id) {
        super("Company", "id", id);
    }
}
