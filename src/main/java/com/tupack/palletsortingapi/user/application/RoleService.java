package com.tupack.palletsortingapi.user.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.RoleNotFoundException;
import com.tupack.palletsortingapi.user.application.dto.CreateRoleRequest;
import com.tupack.palletsortingapi.user.application.dto.RoleDto;
import com.tupack.palletsortingapi.user.application.mapper.RoleMapper;
import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.RoleRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
  private final RoleRepository roleRepository;
  private final RoleMapper roleMapper;

  /**
   * Get all active roles
   */
  public GenericResponse getAllRoles() {
    var data = roleRepository.findAllByEnabled(true)
        .stream()
        .map(roleMapper::toDto)
        .collect(Collectors.toList());
    log.info("Retrieved {} active roles", data.size());
    return GenericResponse.success(data);
  }

  /**
   * Get a role by ID
   */
  public GenericResponse getRoleById(Long id) {
    var role = roleRepository.findById(id)
        .filter(Role::isEnabled)
        .map(roleMapper::toDto);
    log.info("Retrieved role with id: {}", id);
    return role.map(GenericResponse::success)
        .orElseThrow(() -> new RoleNotFoundException(id));
  }

  /**
   * Get a role by name
   */
  public GenericResponse getRoleByName(String name) {
    var role = roleRepository.findByName(name)
        .filter(Role::isEnabled)
        .map(roleMapper::toDto);
    log.info("Retrieved role with name: {}", name);
    return role.map(GenericResponse::success)
        .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + name));
  }

  /**
   * Create a new role
   */
  @Transactional
  public GenericResponse createRole(CreateRoleRequest request) {
    // Validate role name doesn't already exist
    if (roleRepository.findByName(request.getName()).isPresent()) {
      log.warn("Attempt to create role with duplicate name: {}", request.getName());
      throw new BusinessException("Role name already exists", "DUPLICATE_ROLE_NAME");
    }

    Role role = roleMapper.toEntity(request);
    role.setEnabled(true);
    Role saved = roleRepository.save(role);
    log.info("Successfully created role with id: {} and name: {}", saved.getId(), saved.getName());
    return GenericResponse.success(roleMapper.toDto(saved));
  }

  /**
   * Update an existing role
   */
  @Transactional
  public GenericResponse updateRole(Long id, CreateRoleRequest request) {
    return roleRepository.findById(id)
        .map(role -> {
          if (!role.isEnabled()) {
            log.warn("Attempt to update disabled role with id: {}", id);
            throw new BusinessException("Role is disabled", "ROLE_DISABLED");
          }

          // Validate new name doesn't conflict with other roles
          if (!role.getName().equals(request.getName())
              && roleRepository.findByName(request.getName()).isPresent()) {
            log.warn("Attempt to update role {} with duplicate name: {}", id, request.getName());
            throw new BusinessException("Role name already exists", "DUPLICATE_ROLE_NAME");
          }

          roleMapper.updateEntity(request, role);
          Role updated = roleRepository.save(role);
          log.info("Successfully updated role with id: {}", id);
          return GenericResponse.success(roleMapper.toDto(updated));
        })
        .orElseThrow(() -> new RoleNotFoundException(id));
  }

  /**
   * Delete (soft delete) a role
   */
  @Transactional
  public GenericResponse deleteRole(Long id) {
    return roleRepository.findById(id)
        .map(role -> {
          role.setEnabled(false);
          roleRepository.save(role);
          log.info("Successfully deleted (soft) role with id: {}", id);
          return GenericResponse.success("Rol eliminado exitosamente");
        })
        .orElseThrow(() -> new RoleNotFoundException(id));
  }
}

