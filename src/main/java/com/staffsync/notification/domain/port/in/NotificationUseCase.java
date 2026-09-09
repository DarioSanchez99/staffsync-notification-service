package com.staffsync.notification.domain.port.in;

import com.staffsync.notification.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationUseCase {

    List<Notification> findByRecipient(UUID recipientId);

    Notification markAsRead(UUID id);

    void markAllAsRead(UUID recipientId);

    long countUnread(UUID recipientId);
}
