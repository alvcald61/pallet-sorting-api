package com.tupack.palletsortingapi.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // ── Static configuration data (60 min) ───────────────────────────────
        // Roles: essentially immutable at runtime
        manager.registerCustomCache("roles",
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(60, TimeUnit.MINUTES).build());
        manager.registerCustomCache("role",
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(60, TimeUnit.MINUTES).build());

        // Pallets: admin-managed dimensions / types
        manager.registerCustomCache("pallets",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(60, TimeUnit.MINUTES).build());
        manager.registerCustomCache("pallet",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(60, TimeUnit.MINUTES).build());

        // Zones: district→pricing zone mapping, loaded on every order
        manager.registerCustomCache("zones",
            Caffeine.newBuilder().maximumSize(200).expireAfterWrite(60, TimeUnit.MINUTES).build());
        manager.registerCustomCache("zone",
            Caffeine.newBuilder().maximumSize(200).expireAfterWrite(60, TimeUnit.MINUTES).build());
        manager.registerCustomCache("zone-by-district",
            Caffeine.newBuilder().maximumSize(500).expireAfterWrite(60, TimeUnit.MINUTES).build());

        // Price conditions: pricing rules loaded on every cost estimate
        manager.registerCustomCache("price-conditions",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(60, TimeUnit.MINUTES).build());
        manager.registerCustomCache("price-condition",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(60, TimeUnit.MINUTES).build());

        // Companies
        manager.registerCustomCache("companies",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(30, TimeUnit.MINUTES).build());
        manager.registerCustomCache("company",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(30, TimeUnit.MINUTES).build());

        // ── Semi-static operational data (10 min) ────────────────────────────
        // Trucks: can change on assignment, TTL acts as safety net
        manager.registerCustomCache("trucks",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).build());
        manager.registerCustomCache("truck",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).build());

        // Invoice detail and balance: evicted explicitly on payment
        manager.registerCustomCache("invoice",
            Caffeine.newBuilder().maximumSize(500).expireAfterWrite(10, TimeUnit.MINUTES).build());
        manager.registerCustomCache("invoice-balance",
            Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES).build());

        // ── Aggregation / dashboard (5 min) ──────────────────────────────────
        manager.registerCustomCache("dashboard",
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(5, TimeUnit.MINUTES).build());

        return manager;
    }
}
