package com.staffsync.notification.infrastructure.adapter.in.web;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.port.in.NotificationUseCase;
import com.staffsync.notification.infrastructure.adapter.in.web.api.NotificationsApi;
import com.staffsync.notification.infrastructure.adapter.in.web.dto.NotificationResponse;
import com.staffsync.notification.infrastructure.adapter.in.web.dto.NotificationType;
import com.staffsync.notification.infrastructure.adapter.in.web.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationsApi {

    private final NotificationUseCase notificationUseCase;

    @Override
    public ResponseEntity<List<NotificationResponse>> listNotifications(UUID xUserId) {
        List<NotificationResponse> responses = notificationUseCase.findByRecipient(xUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<UnreadCountResponse> countUnreadNotifications(UUID xUserId) {
        long count = notificationUseCase.countUnread(xUserId);
        UnreadCountResponse response = new UnreadCountResponse();
        response.setCount(count);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NotificationResponse> markAsRead(UUID id) {
        Notification notification = notificationUseCase.markAsRead(id);
        return ResponseEntity.ok(toResponse(notification));
    }

    @Override
    public ResponseEntity<Void> markAllAsRead(UUID xUserId) {
        notificationUseCase.markAllAsRead(xUserId);
        return ResponseEntity.noContent().build();
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setRecipientId(notification.getRecipientId());
        response.setType(notification.getType() != null
                ? NotificationType.valueOf(notification.getType().name())
                : null);
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt() != null
                ? notification.getCreatedAt().atOffset(ZoneOffset.UTC)
                : null);
        return response;
    }
}
