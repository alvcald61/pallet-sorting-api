package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.PalletService;
import com.tupack.palletsortingapi.order.application.dto.CreatePalletRequest;
import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PalletDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/pallet")
@RequiredArgsConstructor
public class PalletController {

  private final PalletService palletService;

  /**
   * Get all active pallets
   *
   * @return GenericResponse with list of all active pallets
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllPallets() {
    GenericResponse response = palletService.getAllActivePallets();
    return ResponseEntity.ok(response);
  }

  /**
   * Get a pallet by ID
   *
   * @param id the pallet ID
   * @return GenericResponse with pallet details
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getPalletById(@PathVariable Long id) {
    GenericResponse response = palletService.getPalletById(id);
      return ResponseEntity.ok(response);
  }

  /**
   * Create a new pallet
   *
   * @param request the pallet creation request
   * @return GenericResponse with created pallet
   */
  @PostMapping
//  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> createPallet(@RequestBody CreatePalletRequest request) {
    GenericResponse response = palletService.createPallet(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing pallet
   *
   * @param id the pallet ID
   * @param request the pallet update request
   * @return GenericResponse with updated pallet
   */
  @PutMapping("/{id}")
//  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> updatePallet(@PathVariable Long id,
      @RequestBody CreatePalletRequest request) {
    GenericResponse response = palletService.updatePallet(id, request);
      return ResponseEntity.ok(response);
  }

  /**
   * Delete (soft delete) a pallet
   *
   * @param id the pallet ID
   * @return GenericResponse with success message
   */
  @DeleteMapping("/{id}")
//  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> deletePallet(@PathVariable Long id) {
    GenericResponse response = palletService.deletePallet(id);
      return ResponseEntity.ok(response);
  }

}