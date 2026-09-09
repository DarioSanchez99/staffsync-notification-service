package com.staffsync.notification.domain.port.out;

import com.staffsync.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    List<Notification> findByRecipientId(UUID recipientId);

    Optional<Notification> findById(UUID id);

    List<Notification> findByRecipientIdAndReadFalse(UUID recipientId);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    List<Notification> findAll();
}
