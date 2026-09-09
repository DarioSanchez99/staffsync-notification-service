package com.staffsync.notification.domain.port.out;

import com.staffsync.notification.domain.model.Notification;

public interface NotificationSavePort {

    Notification save(Notification notification);
}
