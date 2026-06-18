package com.tupack.palletsortingapi;

import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class PalletSortingApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(PalletSortingApiApplication.class, args);
  }

}
