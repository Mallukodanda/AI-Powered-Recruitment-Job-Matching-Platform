package com.recruitment.platform.dto;

import com.recruitment.platform.model.Notification;
import com.recruitment.platform.model.NotificationStatus;
import com.recruitment.platform.model.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponseDto {

    private Long id;
    private Long recipientId;
    private String recipientEmail;
    private NotificationType type;
    private NotificationStatus status;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private String eventId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationResponseDto fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        if (notification.getRecipient() != null) {
            dto.setRecipientId(notification.getRecipient().getId());
            dto.setRecipientEmail(notification.getRecipient().getEmail());
        }
        dto.setType(notification.getType());
        dto.setStatus(notification.getStatus());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setReferenceType(notification.getReferenceType());
        dto.setReferenceId(notification.getReferenceId());
        dto.setEventId(notification.getEventId());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
