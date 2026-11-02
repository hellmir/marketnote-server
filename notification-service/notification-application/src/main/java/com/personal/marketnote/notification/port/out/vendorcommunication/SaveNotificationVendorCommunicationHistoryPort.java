package com.personal.marketnote.notification.port.out.vendorcommunication;

import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistory;

public interface SaveNotificationVendorCommunicationHistoryPort {
    NotificationVendorCommunicationHistory save(NotificationVendorCommunicationHistory history);
}
