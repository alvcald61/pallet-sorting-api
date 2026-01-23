package com.tupack.palletsortingapi.order.application;

public interface FileUploader {
  String upload(String filename, byte[] file);
}
