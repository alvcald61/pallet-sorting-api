package com.tupack.palletsortingapi.user.domain;

import com.tupack.palletsortingapi.order.domain.BaseEntity;
import com.tupack.palletsortingapi.user.domain.enums.ClientType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Client extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  private User user;
  private String ruc;
  private String businessName;
  private String phone;
  private String address;
  @Enumerated(EnumType.STRING)
  private ClientType clientType;
  private boolean trust;
}
