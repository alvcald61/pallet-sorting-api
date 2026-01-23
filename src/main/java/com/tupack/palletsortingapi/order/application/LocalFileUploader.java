package com.tupack.palletsortingapi.order.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LocalFileUploader implements FileUploader {

  @Override
  public String upload(String filename, byte[] file) {
    Path path = Paths.get(filename);
    try {
      Files.write(path, file);
      return path.toAbsolutePath().toString();
    } catch (IOException e) {
      throw new RuntimeException("Error uploading file", e);
    }
  }
}
