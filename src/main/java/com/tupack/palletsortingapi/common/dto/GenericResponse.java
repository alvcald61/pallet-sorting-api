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
    return new GenericResponse(data, "OK", HttpStatus.OK.value(), null);
  }

  public static GenericResponse success(Object data, String message) {
    return new GenericResponse(data, message, HttpStatus.OK.value(), null);
  }

  public static GenericResponse created(Object data) {
    return new GenericResponse(data, "Created successfully", HttpStatus.CREATED.value(), null);
  }

  public static GenericResponse created(Object data, String message) {
    return new GenericResponse(data, message, HttpStatus.CREATED.value(), null);
  }

  public static GenericResponse badRequest(String message) {
    return new GenericResponse(null, message, HttpStatus.BAD_REQUEST.value(), null);
  }

  public static GenericResponse notFound(String message) {
    return new GenericResponse(null, message, HttpStatus.NOT_FOUND.value(), null);
  }

  public static GenericResponse error(String message) {
    return new GenericResponse(null, message, HttpStatus.NOT_FOUND.value(), null);
  }

  public static GenericResponse internalError(String message) {
    return new GenericResponse(null, message, HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
  }

  public static GenericResponse unauthorized(String message) {
    return new GenericResponse(null, message, HttpStatus.UNAUTHORIZED.value(), null);
  }

  public static GenericResponse forbidden(String message) {
    return new GenericResponse(null, message, HttpStatus.FORBIDDEN.value(), null);
  }

  public static GenericResponse withPagination(Object data, PageResponse pageInfo) {
    return new GenericResponse(data, "OK", HttpStatus.OK.value(), pageInfo);
  }
}
