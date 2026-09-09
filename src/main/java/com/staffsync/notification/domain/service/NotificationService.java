package com.staffsync.notification.domain.service;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.port.in.NotificationUseCase;
import com.staffsync.notification.domain.port.out.NotificationRepository;
import com.staffsync.notification.domain.port.out.NotificationSavePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationSavePort notificationSavePort;

    @Override
    public List<Notification> findByRecipient(UUID recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    @Override
    public Notification markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + id));
        notification.setRead(true);
        return notificationSavePort.save(notification);
    }

    @Override
    public void markAllAsRead(UUID recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalse(recipientId);
        for (Notification notification : unread) {
            notification.setRead(true);
            notificationSavePort.save(notification);
        }
    }

    @Override
    public long countUnread(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }
}
