package com.personal.marketnote.notification.domain.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationVendorCommunicationHistoryCreateState {
    private final NotificationVendorCommunicationTargetType targetType;
    private final String targetId;
    private final NotificationVendorName vendorName;
    private final NotificationVendorCommunicationType communicationType;
    private final NotificationVendorCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
