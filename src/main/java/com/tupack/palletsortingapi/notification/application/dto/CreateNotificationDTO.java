package com.tupack.palletsortingapi.notification.application.dto;

import com.tupack.palletsortingapi.notification.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationDTO {
    private String title;
    private String message;
    private NotificationType type;
    private String relatedEntityType;
    private String relatedEntityId;
    private String userId;
    private Map<String, Object> metadata;
}
