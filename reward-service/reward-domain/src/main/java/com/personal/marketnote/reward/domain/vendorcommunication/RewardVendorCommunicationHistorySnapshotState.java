package com.personal.marketnote.reward.domain.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RewardVendorCommunicationHistorySnapshotState {
    private final Long id;
    private final RewardVendorCommunicationTargetType targetType;
    private final String targetId;
    private final RewardVendorName vendorName;
    private final RewardVendorCommunicationType communicationType;
    private final RewardVendorCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
    private final LocalDateTime createdAt;
}
