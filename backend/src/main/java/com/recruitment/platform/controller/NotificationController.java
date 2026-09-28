package com.recruitment.platform.controller;

import com.recruitment.platform.dto.NotificationResponseDto;
import com.recruitment.platform.model.NotificationStatus;
import com.recruitment.platform.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "User Alerts, Status Updates, Interview Notifications, and Unread Count")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get list of notifications for the authenticated user (optional status filter)")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @RequestParam(required = false) NotificationStatus status,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(notificationService.getNotificationsForUser(email, status));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count for badge display")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        String email = principal != null ? principal.getName() : null;
        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable Long id,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(notificationService.markAsRead(id, email));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<Map<String, String>> markAllAsRead(Principal principal) {
        String email = principal != null ? principal.getName() : null;
        notificationService.markAllAsRead(email);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
