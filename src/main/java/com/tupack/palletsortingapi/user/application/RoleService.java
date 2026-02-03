package com.tupack.palletsortingapi.user.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.RoleNotFoundException;
import com.tupack.palletsortingapi.user.application.dto.CreateRoleRequest;
import com.tupack.palletsortingapi.user.application.dto.RoleDto;
import com.tupack.palletsortingapi.user.application.mapper.RoleMapper;
import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.RoleRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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
    return GenericResponse.success(data);
  }

  /**
   * Get a role by ID
   */
  public GenericResponse getRoleById(Long id) {
    var role = roleRepository.findById(id)
        .filter(Role::isEnabled)
        .map(roleMapper::toDto);
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
      return GenericResponse.error("El rol ya existe");
    }

    try {
      Role role = roleMapper.toEntity(request);
      role.setEnabled(true);
      Role saved = roleRepository.save(role);
      return GenericResponse.success(roleMapper.toDto(saved));
    } catch (Exception e) {
      return GenericResponse.error("Error al crear el rol: " + e.getMessage());
    }
  }

  /**
   * Update an existing role
   */
  @Transactional
  public GenericResponse updateRole(Long id, CreateRoleRequest request) {
    return roleRepository.findById(id)
        .map(role -> {
          if (!role.isEnabled()) {
            return GenericResponse.error("Rol desactivado");
          }

          // Validate new name doesn't conflict with other roles
          if (!role.getName().equals(request.getName())
              && roleRepository.findByName(request.getName()).isPresent()) {
            return GenericResponse.error("El nombre del rol ya existe");
          }

          roleMapper.updateEntity(request, role);
          Role updated = roleRepository.save(role);
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
          return GenericResponse.success("Rol eliminado exitosamente");
        })
        .orElseThrow(() -> new RoleNotFoundException(id));
  }
}

