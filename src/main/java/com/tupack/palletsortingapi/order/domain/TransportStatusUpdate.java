package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.common.BaseEntity;
import com.tupack.palletsortingapi.order.domain.emuns.TransportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks transport status changes for an order with detailed context.
 * Provides granular tracking of the physical movement and handling of cargo.
 */
@Entity
@Table(name = "transport_status_updates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportStatusUpdate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private TransportStatus status;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "location_latitude")
  private Double locationLatitude;

  @Column(name = "location_longitude")
  private Double locationLongitude;

  @Column(name = "location_address", length = 500)
  private String locationAddress;

  @Column(name = "notes", length = 1000)
  private String notes;

  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  @Column(name = "photo_url", length = 500)
  private String photoUrl;

  @Column(name = "signature_url", length = 500)
  private String signatureUrl;

  /**
   * Constructor for basic status update
   */
  public TransportStatusUpdate(Order order, TransportStatus status) {
    this.order = order;
    this.status = status;
    this.timestamp = LocalDateTime.now();
  }

  /**
   * Constructor with location
   */
  public TransportStatusUpdate(Order order, TransportStatus status, Double latitude,
      Double longitude, String address) {
    this(order, status);
    this.locationLatitude = latitude;
    this.locationLongitude = longitude;
    this.locationAddress = address;
  }
}
