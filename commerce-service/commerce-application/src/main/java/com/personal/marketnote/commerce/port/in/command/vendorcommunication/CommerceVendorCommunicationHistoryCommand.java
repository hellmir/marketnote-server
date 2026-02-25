package com.personal.marketnote.commerce.port.in.command.vendorcommunication;

import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import lombok.Builder;

@Builder
public record CommerceVendorCommunicationHistoryCommand(
        CommerceVendorCommunicationTargetType targetType,
        String targetId,
        CommerceVendorName vendorName,
        CommerceVendorCommunicationType communicationType,
        CommerceVendorCommunicationSenderType sender,
        String exception,
        String payload,
        String payloadJson
) {
}
