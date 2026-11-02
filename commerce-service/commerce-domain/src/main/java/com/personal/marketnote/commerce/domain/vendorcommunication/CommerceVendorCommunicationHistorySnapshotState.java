package com.personal.marketnote.commerce.domain.vendorcommunication;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommerceVendorCommunicationHistorySnapshotState {
    private final Long id;
    private final CommerceVendorCommunicationTargetType targetType;
    private final String targetId;
    private final CommerceVendorName vendorName;
    private final CommerceVendorCommunicationType communicationType;
    private final CommerceVendorCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final String payloadJson;
    private final LocalDateTime createdAt;
}
