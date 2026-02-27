package com.tupack.palletsortingapi.user.domain;

import com.tupack.palletsortingapi.order.domain.Truck;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "driver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long driverId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
  private boolean enabled;
  @OneToOne(fetch = FetchType.LAZY)
  private User user;
  private String dni;
  private String phone;
  private String driverLicence;
  @OneToOne
  private Truck truck;


  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    updatedAt = LocalDateTime.now();
    enabled = true;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
