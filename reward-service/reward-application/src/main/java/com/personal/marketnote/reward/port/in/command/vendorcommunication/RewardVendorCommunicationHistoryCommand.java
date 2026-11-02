package com.personal.marketnote.reward.port.in.command.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RewardVendorCommunicationHistoryCommand {
    private final RewardVendorCommunicationTargetType targetType;
    private final String targetId;
    private final RewardVendorName vendorName;
    private final RewardVendorCommunicationType communicationType;
    private final RewardVendorCommunicationSenderType sender;
    private final String exception;
    private final String payload;
    private final JsonNode payloadJson;
}
