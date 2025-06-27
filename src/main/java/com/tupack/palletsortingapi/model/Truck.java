package com.tupack.palletsortingapi.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;

@Entity
@Getter
@Setter
public class Truck extends BaseEntity {

    private String type;
    private Double width;
    private Double length;
    private Double height;
    private String status;
    private Integer amount;
    private String licensePlate;
    private Double volume;
    private Double weight;
    private Double area;

}
