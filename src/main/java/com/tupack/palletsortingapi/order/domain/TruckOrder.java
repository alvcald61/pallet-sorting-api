package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.id.TruckOrderId;
import com.tupack.palletsortingapi.user.domain.Driver;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "truckOrder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TruckOrder {
  @EmbeddedId
  private TruckOrderId truckOrderId;
  @OneToOne
  @MapsId("orderId")
  private Order order;
  @ManyToOne
  @MapsId("truckId")
  private Truck truck;

  @ManyToOne
  @JoinColumn(name = "driverId", nullable = false)
  private Driver driver;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
  private boolean enabled;

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

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    TruckOrder that = (TruckOrder) o;
    return getTruckOrderId().getOrderId() != null && getTruckOrderId().getTruckId() != null
            && Objects.equals(getTruckOrderId().getOrderId(), that.getTruckOrderId().getOrderId())
            && Objects.equals(getTruckOrderId().getTruckId(), that.getTruckOrderId().getTruckId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
            .getPersistentClass().hashCode() : getClass().hashCode();
  }
}
