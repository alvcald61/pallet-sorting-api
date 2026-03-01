package com.tupack.palletsortingapi.order.infrastructure.outbound.storage;

import com.tupack.palletsortingapi.common.config.CloudflareR2Properties;
import com.tupack.palletsortingapi.common.config.StorageProperties;
import com.tupack.palletsortingapi.common.exception.FileUploadException;
import java.net.URI;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * FileUploader implementation that stores files in Cloudflare R2.
 *
 * <p>Cloudflare R2 exposes an S3-compatible API, so we use the standard AWS SDK v2 pointing at the
 * R2 endpoint: {@code https://<accountId>.r2.cloudflarestorage.com}.
 *
 * <p>This bean is marked {@code @Primary} so Spring injects it wherever a {@link FileUploader} is
 * needed. Remove {@code @Primary} (or set {@code application.cloudflare.r2.enabled=false}) to fall
 * back to {@link LocalFileUploader}.
 */
@Service
@Primary
@Slf4j
public class CloudflareR2FileUploader implements FileUploader {

  private final S3Client s3Client;
  private final CloudflareR2Properties r2Properties;
  private final StorageProperties storageProperties;

  public CloudflareR2FileUploader(
      CloudflareR2Properties r2Properties, StorageProperties storageProperties) {
    this.r2Properties = r2Properties;
    this.storageProperties = storageProperties;
    this.s3Client = buildS3Client(r2Properties);
  }

  @Override
  public String upload(String filename, byte[] file) {
    // 1. Validate file size (reutilizamos la misma lógica que LocalFileUploader)
    if (file.length > storageProperties.getMaxFileSize()) {
      log.warn(
          "File upload rejected: size {} exceeds maximum {}",
          file.length,
          storageProperties.getMaxFileSize());
      throw new FileUploadException("File size exceeds maximum allowed size");
    }

    // 2. Sanitize and validate filename
    String sanitizedFilename = sanitizeFilename(filename);
    validateFileExtension(sanitizedFilename);

    // 3. Generate unique key to avoid collisions
    String objectKey = UUID.randomUUID() + "_" + sanitizedFilename;

    // 4. Detect content type from extension
    String contentType = resolveContentType(sanitizedFilename);

    try {
      PutObjectRequest putRequest =
          PutObjectRequest.builder()
              .bucket(r2Properties.getBucketName())
              .key(objectKey)
              .contentType(contentType)
              .contentLength((long) file.length)
              .build();

      s3Client.putObject(putRequest, RequestBody.fromBytes(file));
      log.info("File uploaded to R2 successfully: key={}", objectKey);

      return buildPublicUrl(objectKey);

    } catch (S3Exception e) {
      log.error("Error uploading file to Cloudflare R2: key={}, code={}", objectKey, e.awsErrorDetails().errorCode(), e);
      throw new FileUploadException("Error uploading file to cloud storage", e);
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static S3Client buildS3Client(CloudflareR2Properties props) {
    String endpoint =
        String.format("https://%s.r2.cloudflarestorage.com", props.getAccountId());

    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        // R2 requiere una región fija (no importa cuál, 'auto' es la recomendada por Cloudflare)
        .region(Region.of("auto"))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())))
        .build();
  }

  /**
   * Returns the publicly accessible URL for the uploaded object. Uses the configured publicUrl
   * base if present; otherwise falls back to the R2 endpoint URL (useful during development).
   */
  private String buildPublicUrl(String objectKey) {
    String base = r2Properties.getPublicUrl();
    if (base != null && !base.isBlank()) {
      return base.endsWith("/") ? base + objectKey : base + "/" + objectKey;
    }
    // Fallback: private endpoint (solo accesible con credenciales)
    return String.format(
        "https://%s.r2.cloudflarestorage.com/%s/%s",
        r2Properties.getAccountId(), r2Properties.getBucketName(), objectKey);
  }

  private String sanitizeFilename(String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      throw new FileUploadException("Filename cannot be empty");
    }
    String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    if (sanitized.startsWith(".")) {
      sanitized = "_" + sanitized;
    }
    if (sanitized.trim().isEmpty()) {
      throw new FileUploadException("Invalid filename");
    }
    return sanitized;
  }

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

  private String resolveContentType(String filename) {
    String ext = getFileExtension(filename).toLowerCase();
    return switch (ext) {
      case "pdf" -> "application/pdf";
      case "xml" -> "application/xml";
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      default -> "application/octet-stream";
    };
  }
}
