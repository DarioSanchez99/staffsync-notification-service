package com.staffsync.notification.service;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.model.NotificationType;
import com.staffsync.notification.domain.port.out.NotificationRepository;
import com.staffsync.notification.domain.port.out.NotificationSavePort;
import com.staffsync.notification.domain.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSavePort notificationSavePort;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, notificationSavePort);
    }

    @Test
    void findByRecipient_returnsList() {
        // Given
        UUID recipientId = UUID.randomUUID();
        Notification n1 = Notification.builder()
                .id(UUID.randomUUID())
                .recipientId(recipientId)
                .type(NotificationType.VACATION_APPROVED)
                .title("Vacation Approved")
                .message("Your vacation has been approved.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        Notification n2 = Notification.builder()
                .id(UUID.randomUUID())
                .recipientId(recipientId)
                .type(NotificationType.VACATION_REJECTED)
                .title("Vacation Rejected")
                .message("Your vacation has been rejected.")
                .read(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByRecipientId(recipientId)).thenReturn(List.of(n1, n2));

        // When
        List<Notification> result = notificationService.findByRecipient(recipientId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Notification::getRecipientId).containsOnly(recipientId);
        verify(notificationRepository).findByRecipientId(recipientId);
    }

    @Test
    void markAsRead_updatesEntity() {
        // Given
        UUID notificationId = UUID.randomUUID();
        Notification existing = Notification.builder()
                .id(notificationId)
                .recipientId(UUID.randomUUID())
                .type(NotificationType.VACATION_APPROVED)
                .title("Vacation Approved")
                .message("Your vacation has been approved.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(existing));
        when(notificationSavePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Notification result = notificationService.markAsRead(notificationId);

        // Then
        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).findById(notificationId);
        verify(notificationSavePort).save(any(Notification.class));
    }

    @Test
    void countUnread_returnsCount() {
        // Given
        UUID recipientId = UUID.randomUUID();
        when(notificationRepository.countByRecipientIdAndReadFalse(recipientId)).thenReturn(3L);

        // When
        long count = notificationService.countUnread(recipientId);

        // Then
        assertThat(count).isEqualTo(3L);
        verify(notificationRepository).countByRecipientIdAndReadFalse(recipientId);
    }
}
