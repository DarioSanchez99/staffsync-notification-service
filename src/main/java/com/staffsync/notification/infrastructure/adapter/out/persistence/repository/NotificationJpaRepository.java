package com.staffsync.notification.infrastructure.adapter.out.persistence.repository;

import com.staffsync.notification.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByRecipientId(UUID recipientId);

    List<NotificationEntity> findByRecipientIdAndReadFalse(UUID recipientId);

    long countByRecipientIdAndReadFalse(UUID recipientId);
}
