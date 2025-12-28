package com.tupack.palletsortingapi.user.application;

import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.user.application.dto.ClientDto;
import com.tupack.palletsortingapi.user.application.dto.CreateClientRequest;
import com.tupack.palletsortingapi.user.application.mapper.ClientMapper;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.ClientRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.RoleRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {
  private final ClientRepository clientRepository;
  private final ClientMapper clientMapper;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Get all active clients
   */
  public GenericResponse getAllClients() {
    var data = clientRepository.findAllByEnabled(true).stream().map(clientMapper::toDto)
        .collect(Collectors.toList());
    return GenericResponse.success(data);
  }

  /**
   * Get a client by ID
   */
  public GenericResponse getClientById(Long id) {
    var client = clientRepository.findById(id).filter(Client::isEnabled).map(clientMapper::toDto);
    return client.map(GenericResponse::success).orElseThrow();
  }

  /**
   * Create a new client and associated user
   * The user will be created with the email from the request
   */
  @Transactional
  public GenericResponse createClient(CreateClientRequest request) {
    // Validate email doesn't already exist
    if (userRepository.existsByEmail(request.getEmail())) {
      return GenericResponse.error("El email ya está registrado");
    }

    try {
      // Create User
      Set<Role> roles = resolveRoles(request.getRoles());
      User user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
          .email(request.getEmail().toLowerCase())
          .password(passwordEncoder.encode(request.getPassword())).roles(roles).enabled(true)
          .build();
      User savedUser = userRepository.save(user);

      // Create Client associated with User
      Client client = Client.builder().user(savedUser).ruc(request.getRuc())
          .businessName(request.getBusinessName()).phone(request.getPhone())
          .address(request.getAddress()).trust(request.isTrust()).enabled(true).build();
      Client savedClient = clientRepository.save(client);

      return GenericResponse.success(clientMapper.toDto(savedClient));
    } catch (Exception e) {
      return GenericResponse.error("Error al crear el cliente: " + e.getMessage());
    }
  }

  /**
   * Update an existing client
   */
  @Transactional
  public GenericResponse updateClient(Long id, CreateClientRequest request) {
    return clientRepository.findById(id).map(client -> {
      if (!client.isEnabled()) {
        return GenericResponse.error("Cliente desactivado");
      }

      // Update user information if needed
      User user = client.getUser();
      if (user != null) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
          user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
      }

      // Update client information
      client.setRuc(request.getRuc());
      client.setBusinessName(request.getBusinessName());
      client.setPhone(request.getPhone());
      client.setAddress(request.getAddress());
      client.setTrust(request.isTrust());
      client.setUser(user);
      Client updated = clientRepository.save(client);

      return GenericResponse.success(clientMapper.toDto(updated));
    }).orElseThrow();
  }

  /**
   * Delete (soft delete) a client
   */
  @Transactional
  public GenericResponse deleteClient(Long id) {
    return clientRepository.findById(id).map(client -> {
      client.setEnabled(false);
      clientRepository.save(client);
      return GenericResponse.success("Cliente eliminado exitosamente");
    }).orElseThrow();
  }

  /**
   * Helper method to resolve roles
   */
  private Set<Role> resolveRoles(List<Long> roleNames) {
    return roleNames.stream().map(id -> roleRepository.findById(id).orElseThrow()
        //            .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()))

    ).collect(Collectors.toSet());
  }
}
