package com.personal.marketnote.commerce.port.out.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import lombok.Builder;

@Builder
public record DeleteBatchKeyPortCommand(
        String batchKey,
        String groupId
) {
    public static DeleteBatchKeyPortCommand from(QuickPaymentCard card) {
        return new DeleteBatchKeyPortCommand(card.getBatchKey(), card.getGroupId());
    }
}
