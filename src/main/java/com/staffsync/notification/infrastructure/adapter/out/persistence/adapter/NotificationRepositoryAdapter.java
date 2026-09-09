package com.staffsync.notification.infrastructure.adapter.out.persistence.adapter;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.domain.port.out.NotificationRepository;
import com.staffsync.notification.domain.port.out.NotificationSavePort;
import com.staffsync.notification.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import com.staffsync.notification.infrastructure.adapter.out.persistence.mapper.NotificationMapper;
import com.staffsync.notification.infrastructure.adapter.out.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository, NotificationSavePort {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = mapper.toEntity(notification);
        NotificationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Notification> findByRecipientId(UUID recipientId) {
        return jpaRepository.findByRecipientId(recipientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByRecipientIdAndReadFalse(UUID recipientId) {
        return jpaRepository.findByRecipientIdAndReadFalse(recipientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByRecipientIdAndReadFalse(UUID recipientId) {
        return jpaRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
