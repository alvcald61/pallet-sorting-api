package com.tupack.palletsortingapi.user.application;

import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.user.application.dto.CreateDriverRequest;
import com.tupack.palletsortingapi.user.application.mapper.DriverMapper;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.DriverRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.RoleRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {
  private final DriverRepository driverRepository;
  private final DriverMapper driverMapper;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Get all active drivers
   */
  public GenericResponse getAllDrivers() {
    var data = driverRepository.findAllByEnabled(true).stream().map(driverMapper::toDto)
        .collect(Collectors.toList());
    return GenericResponse.success(data);
  }

  /**
   * Get a driver by ID
   */
  public GenericResponse getDriverById(Long id) {
    var driver = driverRepository.findById(id).filter(Driver::isEnabled).map(driverMapper::toDto);
    return driver.map(GenericResponse::success).orElseThrow();
  }

  /**
   * Create a new driver and associated user
   * The user will be created with the email from the request
   */
  @Transactional
  public GenericResponse createDriver(CreateDriverRequest request) {
    // Validate email doesn't already exist
    if (userRepository.existsByEmail(request.getEmail())) {
      return GenericResponse.error("El email ya está registrado");
    }

    // Validate DNI doesn't already exist
    if (driverRepository.findByDni(request.getDni()).isPresent()) {
      return GenericResponse.error("El DNI ya está registrado");
    }

    try {
      // Create User
      Set<Role> roles = resolveRoles(request.getRoles());
      User user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
          .email(request.getEmail().toLowerCase())
          .password(passwordEncoder.encode(request.getPassword())).roles(roles).enabled(true)
          .build();
      User savedUser = userRepository.save(user);

      // Create Driver associated with User
      Driver driver = Driver.builder().user(savedUser).dni(request.getDni())
          .phone(request.getPhone()).enabled(true).build();
      Driver savedDriver = driverRepository.save(driver);

      return GenericResponse.success(driverMapper.toDto(savedDriver));
    } catch (Exception e) {
      return GenericResponse.error("Error al crear el conductor: " + e.getMessage());
    }
  }

  /**
   * Update an existing driver
   */
  @Transactional
  public GenericResponse updateDriver(Long id, CreateDriverRequest request) {
    return driverRepository.findById(id).map(driver -> {
      if (!driver.isEnabled()) {
        return GenericResponse.error("Conductor desactivado");
      }

      // Update user information if needed
      User user = driver.getUser();
      if (user != null) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
          user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
      }

      // Update driver information
      driver.setDni(request.getDni());
      driver.setPhone(request.getPhone());
      Driver updated = driverRepository.save(driver);

      return GenericResponse.success(driverMapper.toDto(updated));
    }).orElseThrow();
  }

  /**
   * Delete (soft delete) a driver
   */
  @Transactional
  public GenericResponse deleteDriver(Long id) {
    return driverRepository.findById(id).map(driver -> {
      driver.setEnabled(false);
      driverRepository.save(driver);
      return GenericResponse.success("Conductor eliminado exitosamente");
    }).orElseThrow();
  }

  /**
   * Helper method to resolve roles
   */
  private Set<Role> resolveRoles(List<Long> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) {
      return Set.of();
    }
    return roleIds.stream().map(id -> roleRepository.findById(id).orElseThrow())
        .collect(Collectors.toSet());
  }
}

