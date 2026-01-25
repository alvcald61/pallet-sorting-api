package com.tupack.palletsortingapi.user.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.user.application.ClientService;
import com.tupack.palletsortingapi.user.application.dto.ClientDto;
import com.tupack.palletsortingapi.user.application.dto.CreateClientRequest;
import com.tupack.palletsortingapi.user.domain.Client;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/client")
@RestController
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  /**
   * Get all active clients
   *
   * @return GenericResponse with list of all active clients
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllClients() {
    GenericResponse response = clientService.getAllClients();
    return ResponseEntity.ok(response);
  }

  /**
   * Get a client by ID
   *
   * @param id the client ID
   * @return GenericResponse with client details
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getClientById(@PathVariable Long id) {
    GenericResponse response = clientService.getClientById(id);
      return ResponseEntity.ok(response);
  }

  /**
   * Create a new client and associated user
   *
   * @param request the client creation request with user details
   * @return GenericResponse with created client
   */
  @PostMapping
  public ResponseEntity<GenericResponse> createClient(@RequestBody CreateClientRequest request) {
    GenericResponse response = clientService.createClient(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing client
   *
   * @param id the client ID
   * @param request the client update request
   * @return GenericResponse with updated client
   */
  @PutMapping("/{id}")
  public ResponseEntity<GenericResponse> updateClient(@PathVariable Long id,
      @RequestBody CreateClientRequest request) {
    GenericResponse response = clientService.updateClient(id, request);
      return ResponseEntity.ok(response);
  }

  /**
   * Delete (soft delete) a client
   *
   * @param id the client ID
   * @return GenericResponse with success message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<GenericResponse> deleteClient(@PathVariable Long id) {
    GenericResponse response = clientService.deleteClient(id);
      return ResponseEntity.ok(response);
  }
}
