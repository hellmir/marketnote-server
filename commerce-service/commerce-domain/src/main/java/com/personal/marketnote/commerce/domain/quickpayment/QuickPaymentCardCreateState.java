package com.personal.marketnote.commerce.domain.quickpayment;

import com.personal.marketnote.commerce.domain.payment.MaskedCardNumber;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QuickPaymentCardCreateState {
    private Long userId;
    private String batchKey;
    private String groupId;
    private String cardCode;
    private String cardName;
    private MaskedCardNumber maskedCardNumber;
    private String cardBinType01;
    private String cardBinType02;
}
