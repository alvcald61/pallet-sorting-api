package com.tupack.palletsortingapi.user.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.user.application.RoleService;
import com.tupack.palletsortingapi.user.application.dto.CreateRoleRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

  private final RoleService roleService;

  /**
   * Get all active roles
   *
   * @return GenericResponse with list of all active roles
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllRoles() {
    GenericResponse response = roleService.getAllRoles();
    return ResponseEntity.ok(response);
  }

  /**
   * Get a role by ID
   *
   * @param id the role ID
   * @return GenericResponse with role details
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getRoleById(@PathVariable Long id) {
    GenericResponse response = roleService.getRoleById(id);
      return ResponseEntity.ok(response);
  }

  /**
   * Get a role by name
   *
   * @param name the role name (e.g., ROLE_ADMIN, ROLE_CLIENT)
   * @return GenericResponse with role details
   */
  @GetMapping("/search")
  public ResponseEntity<GenericResponse> getRoleByName(@RequestParam String name) {
    GenericResponse response = roleService.getRoleByName(name);
      return ResponseEntity.ok(response);
  }

  /**
   * Create a new role
   *
   * @param request the role creation request
   * @return GenericResponse with created role
   */
  @PostMapping
  public ResponseEntity<GenericResponse> createRole(@RequestBody CreateRoleRequest request) {
    GenericResponse response = roleService.createRole(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing role
   *
   * @param id the role ID
   * @param request the role update request
   * @return GenericResponse with updated role
   */
  @PutMapping("/{id}")
  public ResponseEntity<GenericResponse> updateRole(@PathVariable Long id,
      @RequestBody CreateRoleRequest request) {
    GenericResponse response = roleService.updateRole(id, request);
      return ResponseEntity.ok(response);
  }

  /**
   * Delete (soft delete) a role
   *
   * @param id the role ID
   * @return GenericResponse with success message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<GenericResponse> deleteRole(@PathVariable Long id) {
    GenericResponse response = roleService.deleteRole(id);
      return ResponseEntity.ok(response);
  }
}
