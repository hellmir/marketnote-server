package com.personal.marketnote.notification.port.in.command.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationVendorCommunicationHistoryCommand {
    private final NotificationVendorCommunicationTargetType targetType;
    private final String targetId;
    private final NotificationVendorName vendorName;
    private final NotificationVendorCommunicationType communicationType;
    private final NotificationVendorCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
