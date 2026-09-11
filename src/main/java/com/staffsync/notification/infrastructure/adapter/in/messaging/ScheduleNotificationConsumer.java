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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleNotificationConsumer {

    private final NotificationSavePort notificationSavePort;
    private final SseEmitterRegistry sseEmitterRegistry;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "staffsync.schedule.notifications")
    public void handleScheduleEvent(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            String weekStart = (String) event.get("weekStart");
            Object employeeIdsObj = event.get("employeeIds");

            if (employeeIdsObj == null) {
                log.warn("Ignoring schedule event with no employeeIds, type: {}", type);
                return;
            }

            if (!"SCHEDULE_PUBLISHED".equals(type) && !"SCHEDULE_WEEK_PUBLISHED".equals(type)) {
                log.debug("Ignoring schedule event type: {}", type);
                return;
            }

            List<String> employeeIds = (List<String>) employeeIdsObj;
            Object shiftCountObj = event.get("shiftCount");
            int shiftCount = shiftCountObj instanceof Number ? ((Number) shiftCountObj).intValue() : 0;

            String title = "SCHEDULE_WEEK_PUBLISHED".equals(type) ? "Weekly Schedule Published" : "New Schedule Published";
            String message;
            if ("SCHEDULE_WEEK_PUBLISHED".equals(type) && weekStart != null) {
                message = shiftCount > 0
                        ? "Your schedule for the week of " + weekStart + " is now available. You have " + shiftCount + " shifts."
                        : "Your schedule for the week of " + weekStart + " has been published.";
            } else {
                message = weekStart != null
                        ? "Your schedule for the week of " + weekStart + " has been published."
                        : "A new schedule has been published.";
            }

            for (String employeeId : employeeIds) {
                Notification notification = Notification.builder()
                        .id(UUID.randomUUID())
                        .recipientId(UUID.fromString(employeeId))
                        .type(NotificationType.SCHEDULE_PUBLISHED)
                        .title(title)
                        .message(message)
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationSavePort.save(notification);
                sseEmitterRegistry.sendToUser(notification.getRecipientId(), Map.of("type", "NOTIFICATION"));
            }

            log.info("Saved schedule notifications for {} employees", employeeIds.size());
        } catch (Exception e) {
            log.error("Failed to process schedule event: {}", e.getMessage(), e);
        }
    }
}
