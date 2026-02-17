package com.tupack.palletsortingapi.notification.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OneSignalService {

    private static final String ONESIGNAL_API_URL = "https://onesignal.com/api/v1/notifications";

    @Value("${application.onesignal.app-id}")
    private String appId;

    @Value("${application.onesignal.rest-api-key}")
    private String restApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send push notification to a single user using external_user_id
     */
    public void sendPushNotification(String userId, String title, String message, Map<String, Object> data) {
        try {
            if (restApiKey == null || restApiKey.isEmpty()) {
                log.warn("OneSignal REST API key not configured. Skipping push notification.");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("app_id", appId);
            payload.put("include_external_user_ids", List.of(userId));

            Map<String, String> headings = new HashMap<>();
            headings.put("en", title);
            payload.put("headings", headings);

            Map<String, String> contents = new HashMap<>();
            contents.put("en", message);
            payload.put("contents", contents);

            if (data != null && !data.isEmpty()) {
                payload.put("data", data);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + restApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(ONESIGNAL_API_URL, request, Map.class);
            log.info("Push notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending push notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send push notification to multiple users
     */
    public void sendPushNotificationToMultiple(List<String> userIds, String title, String message, Map<String, Object> data) {
        try {
            if (restApiKey == null || restApiKey.isEmpty()) {
                log.warn("OneSignal REST API key not configured. Skipping push notification.");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("app_id", appId);
            payload.put("include_external_user_ids", userIds);

            Map<String, String> headings = new HashMap<>();
            headings.put("en", title);
            payload.put("headings", headings);

            Map<String, String> contents = new HashMap<>();
            contents.put("en", message);
            payload.put("contents", contents);

            if (data != null && !data.isEmpty()) {
                payload.put("data", data);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + restApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(ONESIGNAL_API_URL, request, Map.class);
            log.info("Push notification sent to {} users", userIds.size());
        } catch (Exception e) {
            log.error("Error sending push notification to multiple users: {}", e.getMessage());
        }
    }
}
