package com.staffsync.notification.infrastructure.adapter.in.web;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.port.in.NotificationUseCase;
import com.staffsync.notification.infrastructure.adapter.in.web.api.NotificationsApi;
import com.staffsync.notification.infrastructure.adapter.in.web.dto.NotificationResponse;
import com.staffsync.notification.infrastructure.adapter.in.web.dto.NotificationType;
import com.staffsync.notification.infrastructure.adapter.in.web.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationsApi {

    private final NotificationUseCase notificationUseCase;
    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * SSE stream: frontend connects with ?token=<jwt>; gateway extracts X-User-Id header
     * and passes it; we subscribe the user to push events.
     * The token param is ignored here — gateway already validated it and set X-User-Id.
     */
    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestParam(value = "token", required = false) String token) {
        if (userId == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new IllegalArgumentException("Missing X-User-Id"));
            return emitter;
        }
        return sseEmitterRegistry.subscribe(userId);
    }

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
