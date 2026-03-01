package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.user.domain.Client;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dispatcher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Dispatcher extends BaseEntity {
  private String firstName;
  private String lastName;
  private String phone;

  @ManyToOne
  @JoinColumn(name = "clientId")
  private Client client;
}
