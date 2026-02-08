package com.tupack.palletsortingapi.user.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.DriverNotFoundException;
import com.tupack.palletsortingapi.common.exception.DuplicateDniException;
import com.tupack.palletsortingapi.common.exception.DuplicateEmailException;
import com.tupack.palletsortingapi.common.exception.RoleNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
    log.info("Retrieved {} active drivers", data.size());
    return GenericResponse.success(data);
  }

  /**
   * Get a driver by ID
   */
  public GenericResponse getDriverById(Long id) {
    var driver = driverRepository.findById(id).filter(Driver::isEnabled).map(driverMapper::toDto);
    log.info("Retrieved driver with id: {}", id);
    return driver.map(GenericResponse::success)
        .orElseThrow(() -> new DriverNotFoundException(id));
  }

  /**
   * Create a new driver and associated user
   * The user will be created with the email from the request
   */
  @Transactional
  public GenericResponse createDriver(CreateDriverRequest request) {
    // Validate email doesn't already exist
    if (userRepository.existsByEmail(request.getEmail())) {
      log.warn("Attempt to create driver with duplicate email: {}", request.getEmail());
      throw new DuplicateEmailException(request.getEmail());
    }

    // Validate DNI doesn't already exist
    if (driverRepository.findByDni(request.getDni()).isPresent()) {
      log.warn("Attempt to create driver with duplicate DNI: {}", request.getDni());
      throw new DuplicateDniException(request.getDni());
    }

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

    log.info("Successfully created driver with id: {} and email: {}", savedDriver.getDriverId(),
        request.getEmail());
    return GenericResponse.success(driverMapper.toDto(savedDriver));
  }

  /**
   * Update an existing driver
   */
  @Transactional
  public GenericResponse updateDriver(Long id, CreateDriverRequest request) {
    return driverRepository.findById(id).map(driver -> {
      if (!driver.isEnabled()) {
        log.warn("Attempt to update disabled driver with id: {}", id);
        throw new BusinessException("Driver is disabled", "DRIVER_DISABLED");
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

      log.info("Successfully updated driver with id: {}", id);
      return GenericResponse.success(driverMapper.toDto(updated));
    }).orElseThrow(() -> new DriverNotFoundException(id));
  }

  /**
   * Delete (soft delete) a driver
   */
  @Transactional
  public GenericResponse deleteDriver(Long id) {
    return driverRepository.findById(id).map(driver -> {
      driver.setEnabled(false);
      driverRepository.save(driver);
      log.info("Successfully deleted (soft) driver with id: {}", id);
      return GenericResponse.success("Conductor eliminado exitosamente");
    }).orElseThrow(() -> new DriverNotFoundException(id));
  }

  /**
   * Helper method to resolve roles
   */
  private Set<Role> resolveRoles(List<Long> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) {
      return Set.of();
    }
    return roleIds.stream()
        .map(id -> roleRepository.findById(id)
            .orElseThrow(() -> new RoleNotFoundException(id)))
        .collect(Collectors.toSet());
  }
}

