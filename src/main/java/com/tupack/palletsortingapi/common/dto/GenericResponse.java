package com.tupack.palletsortingapi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericResponse {
  private Object data;
  private String message;
  private int statusCode;
  private PageResponse pageInfo;

  public static GenericResponse success(Object data) {
    return new GenericResponse(data, "OK", 200, null);
  }

  public static GenericResponse error(String truckNotFound) {
    return new GenericResponse(null, truckNotFound, 404, null);
  }
}
