package com.staffsync.notification.infrastructure.adapter.in.messaging;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.model.NotificationType;
import com.staffsync.notification.domain.port.out.NotificationSavePort;
import com.staffsync.notification.infrastructure.adapter.in.web.SseEmitterRegistry;
import com.staffsync.notification.infrastructure.adapter.out.client.EmployeeClient;
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
    private final EmployeeClient employeeClient;

    @RabbitListener(queues = "staffsync.vacation.notifications")
    public void handleVacationEvent(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            String employeeIdStr = (String) event.get("employeeId");
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
                String reviewer = (String) event.get("reviewedByName");
                message = reviewer != null
                        ? String.format("Your vacation request from %s to %s has been approved by %s.", startDate, endDate, reviewer)
                        : String.format("Your vacation request from %s to %s has been approved.", startDate, endDate);
            } else if ("VACATION_REJECTED".equals(type)) {
                notificationType = NotificationType.VACATION_REJECTED;
                title = "Vacation Request Rejected";
                String reviewer = (String) event.get("reviewedByName");
                message = reviewer != null
                        ? String.format("Your vacation request from %s to %s has been rejected by %s.", startDate, endDate, reviewer)
                        : String.format("Your vacation request from %s to %s has been rejected.", startDate, endDate);
            } else {
                log.warn("Unknown vacation event type: {}", type);
                return;
            }

            UUID employeeId = UUID.fromString(employeeIdStr);
            UUID recipientId = employeeClient.resolveUserId(employeeId).orElse(employeeId);

            Notification notification = Notification.builder()
                    .id(UUID.randomUUID())
                    .recipientId(recipientId)
                    .type(notificationType)
                    .title(title)
                    .message(message)
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationSavePort.save(notification);
            sseEmitterRegistry.sendToUser(recipientId, Map.of("type", "NOTIFICATION"));
            log.info("Saved notification of type {} for employee {} (userId={})", type, employeeId, recipientId);
        } catch (Exception e) {
            log.error("Failed to process vacation event: {}", e.getMessage(), e);
        }
    }
}
