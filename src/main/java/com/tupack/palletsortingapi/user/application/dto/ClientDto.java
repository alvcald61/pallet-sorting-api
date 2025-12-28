package com.tupack.palletsortingapi.user.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;

/**
 * DTO for {@link com.tupack.palletsortingapi.user.domain.Client}
 */
@Value
public class ClientDto implements Serializable {
  Long id;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
  String ruc;
  String businessName;
  String phone;
  String address;
  String email;
  String firstName;
  String lastName;
  boolean trust;
  List<RoleDto> roles;

}