package com.tupack.palletsortingapi.utils;

import lombok.Getter;

@Getter
public enum PackingType {
  TWO_DIMENSIONAL("2D"), THREE_DIMENSIONAL("3D"), BULK("BULK");
  private final String name;

  PackingType(String name) {
    this.name = name;
  }

}
