package com.tupack.palletsortingapi.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.cloudflare.r2")
@Data
public class CloudflareR2Properties {
  /** Account ID de Cloudflare (visible en el dashboard de R2) */
  private String accountId;
  /** Access Key ID generado en el dashboard de R2 → API Tokens */
  private String accessKeyId;
  /** Secret Access Key generado en el dashboard de R2 → API Tokens */
  private String secretAccessKey;
  /** Nombre del bucket creado en R2 */
  private String bucketName;
  /** URL pública del bucket (opcional, si tienes dominio custom o R2.dev habilitado) */
  private String publicUrl;
}
