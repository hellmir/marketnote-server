package com.personal.marketnote.commerce.port.in.result.quickpayment;

import com.personal.marketnote.commerce.domain.payment.MaskedCardNumber;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import lombok.Builder;

@Builder
public record IssueBatchKeyResult(
        Long quickPaymentCardId,
        String cardCode,
        String cardName,
        String maskedCardNumber,
        String cardBinType01,
        String cardBinType02
) {
    public static IssueBatchKeyResult from(QuickPaymentCard savedCard) {
        return new IssueBatchKeyResult(
                savedCard.getId(),
                savedCard.getCardCode(),
                savedCard.getCardName(),
                MaskedCardNumber.toNullableValue(savedCard.getMaskedCardNumber()),
                savedCard.getCardBinType01(),
                savedCard.getCardBinType02()
        );
    }
}
