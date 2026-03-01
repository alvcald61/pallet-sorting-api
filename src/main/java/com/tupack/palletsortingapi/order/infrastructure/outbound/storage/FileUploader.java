package com.tupack.palletsortingapi.order.infrastructure.outbound.storage;

public interface FileUploader {
  String upload(String filename, byte[] file);
}
