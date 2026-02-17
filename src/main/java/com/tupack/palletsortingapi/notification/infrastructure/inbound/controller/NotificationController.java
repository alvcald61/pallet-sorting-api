package com.tupack.palletsortingapi.notification.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.dto.PageResponse;
import com.tupack.palletsortingapi.notification.application.dto.NotificationDTO;
import com.tupack.palletsortingapi.notification.application.dto.UnreadCountDTO;
import com.tupack.palletsortingapi.notification.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<GenericResponse> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        String userId = getUserId(userDetails);
        PageResponse<NotificationDTO> notifications = notificationService.getUserNotifications(userId, page, size
            , unreadOnly);
        return ResponseEntity.ok(GenericResponse.success(notifications));
    }

    @GetMapping("/count/unread")
    public ResponseEntity<GenericResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        UnreadCountDTO count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(GenericResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<GenericResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        NotificationDTO notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(GenericResponse.success(notification));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<GenericResponse> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(GenericResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(GenericResponse.success(null));
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<GenericResponse> clearAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        notificationService.clearAllNotifications(userId);
        return ResponseEntity.ok(GenericResponse.success(null));
    }

    private String getUserId(UserDetails userDetails) {
        if (userDetails instanceof com.tupack.palletsortingapi.user.domain.User) {
            return String.valueOf(((com.tupack.palletsortingapi.user.domain.User) userDetails).getId());
        }
        return userDetails.getUsername();
    }
}
