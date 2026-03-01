package com.tupack.palletsortingapi.order.infrastructure.outbound.storage;

import com.tupack.palletsortingapi.common.config.StorageProperties;
import com.tupack.palletsortingapi.common.exception.FileUploadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileUploader implements FileUploader {

  private final StorageProperties storageProperties;

  @Override
  public String upload(String filename, byte[] file) {
    // Validate file size
    if (file.length > storageProperties.getMaxFileSize()) {
      log.warn("File upload rejected: size {} exceeds maximum {}", file.length, storageProperties.getMaxFileSize());
      throw new FileUploadException("File size exceeds maximum allowed size");
    }

    // Sanitize and validate filename
    String sanitizedFilename = sanitizeFilename(filename);
    validateFileExtension(sanitizedFilename);

    // Generate unique filename to prevent collisions and further enhance security
    String uniqueFilename = UUID.randomUUID() + "_" + sanitizedFilename;

    // Resolve paths and validate no path traversal
    Path baseDir = Paths.get(storageProperties.getBasePath()).toAbsolutePath().normalize();
    Path targetPath = baseDir.resolve(uniqueFilename).normalize();

    // Critical security check: ensure resolved path is within base directory
    if (!targetPath.startsWith(baseDir)) {
      log.error("Path traversal attempt detected: {} -> {}", filename, targetPath);
      throw new FileUploadException("Invalid file path");
    }

    try {
      // Create base directory if it doesn't exist
      Files.createDirectories(baseDir);

      // Write file
      Files.write(targetPath, file, StandardOpenOption.CREATE_NEW);
      log.info("File uploaded successfully: {}", uniqueFilename);

      return targetPath.toString();
    } catch (IOException e) {
      log.error("Error uploading file: {}", filename, e);
      throw new FileUploadException("Error uploading file", e);
    }
  }

  /**
   * Sanitize filename by removing dangerous characters and path separators
   */
  private String sanitizeFilename(String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      throw new FileUploadException("Filename cannot be empty");
    }

    // Remove path separators and dangerous characters
    String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

    // Prevent hidden files
    if (sanitized.startsWith(".")) {
      sanitized = "_" + sanitized;
    }

    // Prevent empty filename after sanitization
    if (sanitized.trim().isEmpty()) {
      throw new FileUploadException("Invalid filename");
    }

    return sanitized;
  }

  /**
   * Validate file extension against whitelist
   */
  private void validateFileExtension(String filename) {
    String extension = getFileExtension(filename);

    if (extension.isEmpty()) {
      throw new FileUploadException("File must have an extension");
    }

    if (!storageProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
      log.warn("File upload rejected: extension '{}' not in whitelist", extension);
      throw new FileUploadException("File type not allowed: " + extension);
    }
  }

  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      return "";
    }
    return filename.substring(lastDotIndex + 1);
  }
}
