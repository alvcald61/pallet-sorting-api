package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.AddressValidationRequest;
import com.tupack.palletsortingapi.order.application.dto.AddressValidationResponse;
import com.tupack.palletsortingapi.order.application.service.AddressValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Address", description = "Address validation and geocoding endpoints")
public class AddressController {

  private final AddressValidationService addressValidationService;

  @Operation(
      summary = "Validate address",
      description = "Validates and normalizes an address, returning coordinates and confidence level. Prepared for Google Maps API integration."
  )
  @PostMapping("/validate")
  public ResponseEntity<GenericResponse> validateAddress(
      @Valid @RequestBody AddressValidationRequest request) {
    AddressValidationResponse validation = addressValidationService.validateAddress(request);
    return ResponseEntity.ok(GenericResponse.builder()
        .message("OK").statusCode(200)
        .data(validation)
        .build());
  }
}
