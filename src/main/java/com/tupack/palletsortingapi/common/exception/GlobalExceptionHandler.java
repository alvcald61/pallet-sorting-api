package com.tupack.palletsortingapi.common.exception;

import com.tupack.palletsortingapi.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for all REST controllers. Provides consistent error responses across the
 * API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handle ResourceNotFoundException (404).
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex,
      HttpServletRequest request) {

    logger.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        ex.getMessage(),
        request.getRequestURI()
    ).withErrorCode("RESOURCE_NOT_FOUND");

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handle BusinessException (400).
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      BusinessException ex,
      HttpServletRequest request) {

    logger.warn("Business exception: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        ex.getMessage(),
        request.getRequestURI()
    ).withErrorCode(ex.getErrorCode());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle validation errors from @Valid annotations (400).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {

    logger.warn("Validation error: {}", ex.getMessage());

    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "Validation Failed",
        "One or more fields have validation errors",
        request.getRequestURI()
    ).withErrorCode("VALIDATION_ERROR").withValidationErrors(validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle type mismatch errors (400).
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex,
      HttpServletRequest request) {

    logger.warn("Type mismatch: {}", ex.getMessage());

    String message = String.format(
        "Invalid value '%s' for parameter '%s'. Expected type: %s",
        ex.getValue(),
        ex.getName(),
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
    );

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        message,
        request.getRequestURI()
    ).withErrorCode("TYPE_MISMATCH");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle authentication errors (401).
   */
  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      Exception ex,
      HttpServletRequest request) {

    logger.warn("Authentication error: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.UNAUTHORIZED.value(),
        "Unauthorized",
        "Invalid credentials or authentication failed",
        request.getRequestURI()
    ).withErrorCode("AUTHENTICATION_FAILED");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handle access denied errors (403).
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex,
      HttpServletRequest request) {

    logger.warn("Access denied: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.FORBIDDEN.value(),
        "Forbidden",
        "You don't have permission to access this resource",
        request.getRequestURI()
    ).withErrorCode("ACCESS_DENIED");

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Handle FileUploadException (400).
   */
  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<ErrorResponse> handleFileUploadException(
      FileUploadException ex,
      HttpServletRequest request) {

    logger.warn("File upload error: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "File Upload Failed",
        ex.getMessage(),
        request.getRequestURI()
    ).withErrorCode("FILE_UPLOAD_ERROR");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle IllegalArgumentException (400).
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex,
      HttpServletRequest request) {

    logger.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        ex.getMessage(),
        request.getRequestURI()
    ).withErrorCode("INVALID_ARGUMENT");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle IllegalStateException (409 Conflict).
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(
      IllegalStateException ex,
      HttpServletRequest request) {

    logger.warn("Illegal state: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.CONFLICT.value(),
        "Conflict",
        ex.getMessage(),
        request.getRequestURI()
    ).withErrorCode("ILLEGAL_STATE");

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handle all other unexpected exceptions (500).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex,
      HttpServletRequest request) {

    logger.error("Unexpected error occurred", ex);

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        "An unexpected error occurred. Please contact support if the problem persists.",
        request.getRequestURI()
    ).withErrorCode("INTERNAL_ERROR");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
