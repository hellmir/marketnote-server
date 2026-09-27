package com.personal.marketnote.commerce.domain.quickpayment;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.commerce.domain.payment.MaskedCardNumber;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QuickPaymentCardSnapshotState {
    private Long id;
    private Long userId;
    private String batchKey;
    private String groupId;
    private String cardCode;
    private String cardName;
    private MaskedCardNumber maskedCardNumber;
    private String cardBinType01;
    private String cardBinType02;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
