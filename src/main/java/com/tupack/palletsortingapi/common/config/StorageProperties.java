package com.tupack.palletsortingapi.common.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.storage")
@Data
public class StorageProperties {
  private String basePath;
  private List<String> allowedExtensions;
  private Long maxFileSize;
}
