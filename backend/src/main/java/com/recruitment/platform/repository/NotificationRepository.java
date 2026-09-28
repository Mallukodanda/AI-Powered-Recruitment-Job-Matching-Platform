package com.recruitment.platform.repository;

import com.recruitment.platform.model.Notification;
import com.recruitment.platform.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(Long recipientId, NotificationStatus status);

    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    boolean existsByEventIdAndRecipientId(String eventId, Long recipientId);
}
