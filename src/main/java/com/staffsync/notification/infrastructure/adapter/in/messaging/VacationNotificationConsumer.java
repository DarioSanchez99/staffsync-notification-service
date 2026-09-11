package com.staffsync.notification.infrastructure.adapter.in.messaging;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.model.NotificationType;
import com.staffsync.notification.domain.port.out.NotificationSavePort;
import com.staffsync.notification.infrastructure.adapter.in.web.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacationNotificationConsumer {

    private final NotificationSavePort notificationSavePort;
    private final SseEmitterRegistry sseEmitterRegistry;

    @RabbitListener(queues = "staffsync.vacation.notifications")
    public void handleVacationEvent(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            String employeeId = (String) event.get("employeeId");
            String startDate = (String) event.get("startDate");
            String endDate = (String) event.get("endDate");

            NotificationType notificationType;
            String title;
            String message;

            if ("VACATION_REQUESTED".equals(type)) {
                notificationType = NotificationType.VACATION_REQUESTED;
                title = "Vacation Request Submitted";
                message = String.format("Your vacation request from %s to %s is pending approval.", startDate, endDate);
            } else if ("VACATION_APPROVED".equals(type)) {
                notificationType = NotificationType.VACATION_APPROVED;
                title = "Vacation Request Approved";
                message = String.format("Your vacation request from %s to %s has been approved.", startDate, endDate);
            } else if ("VACATION_REJECTED".equals(type)) {
                notificationType = NotificationType.VACATION_REJECTED;
                title = "Vacation Request Rejected";
                message = String.format("Your vacation request from %s to %s has been rejected.", startDate, endDate);
            } else {
                log.warn("Unknown vacation event type: {}", type);
                return;
            }

            Notification notification = Notification.builder()
                    .id(UUID.randomUUID())
                    .recipientId(UUID.fromString(employeeId))
                    .type(notificationType)
                    .title(title)
                    .message(message)
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationSavePort.save(notification);
            sseEmitterRegistry.sendToUser(notification.getRecipientId(), Map.of("type", "NOTIFICATION"));
            log.info("Saved notification of type {} for employee {}", type, employeeId);
        } catch (Exception e) {
            log.error("Failed to process vacation event: {}", e.getMessage(), e);
        }
    }
}
