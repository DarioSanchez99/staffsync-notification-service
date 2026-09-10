package com.staffsync.notification.infrastructure.adapter.in.messaging;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.model.NotificationType;
import com.staffsync.notification.domain.port.out.NotificationSavePort;
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

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "staffsync.schedule.notifications")
    public void handleScheduleEvent(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            String weekStart = (String) event.get("weekStart");
            Object employeeIdsObj = event.get("employeeIds");

            if (!"SCHEDULE_PUBLISHED".equals(type) || employeeIdsObj == null) {
                log.warn("Ignoring schedule event type: {}", type);
                return;
            }

            List<String> employeeIds = (List<String>) employeeIdsObj;
            String title = "New Schedule Published";
            String message = weekStart != null
                    ? "Your schedule for the week of " + weekStart + " has been published."
                    : "A new schedule has been published.";

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
            }

            log.info("Saved schedule notifications for {} employees", employeeIds.size());
        } catch (Exception e) {
            log.error("Failed to process schedule event: {}", e.getMessage(), e);
        }
    }
}
