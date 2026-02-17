package com.tupack.palletsortingapi.notification.application.service;

import com.tupack.palletsortingapi.common.dto.PageResponse;
import com.tupack.palletsortingapi.notification.application.dto.CreateNotificationDTO;
import com.tupack.palletsortingapi.notification.application.dto.NotificationDTO;
import com.tupack.palletsortingapi.notification.application.dto.UnreadCountDTO;
import com.tupack.palletsortingapi.notification.domain.Notification;
import com.tupack.palletsortingapi.notification.infrastructure.outbound.database.NotificationRepository;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OneSignalService oneSignalService;

    /**
     * Create a notification and send push notification
     */
    @Transactional
    public NotificationDTO createNotification(CreateNotificationDTO dto) {
        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType())
                .relatedEntityType(dto.getRelatedEntityType())
                .relatedEntityId(dto.getRelatedEntityId())
                .userId(dto.getUserId())
                .isRead(false)
                .metadata(dto.getMetadata())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send push notification
        Map<String, Object> pushData = new HashMap<>();
        if (dto.getRelatedEntityType() != null) {
            pushData.put("entityType", dto.getRelatedEntityType());
        }
        if (dto.getRelatedEntityId() != null) {
            pushData.put("entityId", dto.getRelatedEntityId());
        }
        oneSignalService.sendPushNotification(dto.getUserId(), dto.getTitle(), dto.getMessage(), pushData);

        log.info("Notification created for user: {}", dto.getUserId());
        return toDTO(saved);
    }

    /**
     * Create notifications for multiple users
     */
    @Transactional
    public void createNotificationForMultipleUsers(List<String> userIds, CreateNotificationDTO baseDto) {
        List<Notification> notifications = userIds.stream()
                .map(userId -> Notification.builder()
                        .title(baseDto.getTitle())
                        .message(baseDto.getMessage())
                        .type(baseDto.getType())
                        .relatedEntityType(baseDto.getRelatedEntityType())
                        .relatedEntityId(baseDto.getRelatedEntityId())
                        .userId(userId)
                        .isRead(false)
                        .metadata(baseDto.getMetadata())
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        // Send push notifications
        Map<String, Object> pushData = new HashMap<>();
        if (baseDto.getRelatedEntityType() != null) {
            pushData.put("entityType", baseDto.getRelatedEntityType());
        }
        if (baseDto.getRelatedEntityId() != null) {
            pushData.put("entityId", baseDto.getRelatedEntityId());
        }
        oneSignalService.sendPushNotificationToMultiple(userIds, baseDto.getTitle(), baseDto.getMessage(), pushData);

        log.info("Notifications created for {} users", userIds.size());
    }

    /**
     * Get paginated notifications for a user
     */
    public PageResponse<NotificationDTO> getUserNotifications(String userId, int page, int size, boolean unreadOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage = notificationRepository.findByUserIdWithFilter(userId, unreadOnly, pageable);

        List<NotificationDTO> notifications = notificationPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<NotificationDTO>builder()
                .data(notifications)
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .currentPage(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .build();
    }

    /**
     * Get unread count for a user
     */
    public UnreadCountDTO getUnreadCount(String userId) {
        long count = notificationRepository.countUnreadByUserId(userId);
        return UnreadCountDTO.builder().count(count).build();
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationDTO markAsRead(Long id, String userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification updated = notificationRepository.save(notification);

        log.info("Notification {} marked as read", id);
        return toDTO(updated);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(String userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Notification> notifications = notificationRepository.findByUserIdWithFilter(userId, true, pageable);

        notifications.getContent().forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(notifications.getContent());
        log.info("All notifications marked as read for user: {}", userId);
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long id, String userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted", id);
    }

    /**
     * Clear all notifications for a user
     */
    @Transactional
    public void clearAllNotifications(String userId) {
        notificationRepository.deleteAllByUserId(userId);
        log.info("All notifications cleared for user: {}", userId);
    }

    /**
     * Get all users with a specific role
     */
    public List<String> getUserIdsByRole(String roleName) {
        List<User> users = userRepository.findByRoles_Name(roleName);
        return users.stream().map(User::getId).collect(Collectors.toList());
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .metadata(notification.getMetadata())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
