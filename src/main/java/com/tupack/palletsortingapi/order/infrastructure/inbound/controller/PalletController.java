package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pallets")
public class PalletController {

  @GetMapping("/public")
  public String publicEndpoint() {
    return "endpoint público OK";
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin")
  public String adminOnly() {
    return "solo ADMIN puede ver esto";
  }
}