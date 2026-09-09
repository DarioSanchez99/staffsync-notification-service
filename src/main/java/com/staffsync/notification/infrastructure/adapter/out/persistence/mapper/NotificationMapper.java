package com.staffsync.notification.infrastructure.adapter.out.persistence.mapper;

import com.staffsync.notification.domain.model.Notification;
import com.staffsync.notification.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationEntity toEntity(Notification notification);

    Notification toDomain(NotificationEntity entity);
}
