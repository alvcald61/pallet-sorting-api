package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.enums.TruckStatus;
import com.tupack.palletsortingapi.user.domain.Driver;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Truck extends BaseEntity {

  private String type;
  private Double width;
  private Double length;
  private Double height;
  @Enumerated(EnumType.STRING)
  private TruckStatus status;
  private String licensePlate;
  private Double volume;
  private Double weight;
  private Double area;
  private String truckType;
  private Double multiplayer;
  @OneToOne
  private Driver driver;
  @OneToMany(mappedBy = "truck")
  private List<Order> orders;

}
