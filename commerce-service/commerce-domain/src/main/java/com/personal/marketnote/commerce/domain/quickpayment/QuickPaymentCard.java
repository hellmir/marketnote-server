package com.personal.marketnote.commerce.domain.quickpayment;

import com.personal.marketnote.common.domain.BaseDomain;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class QuickPaymentCard extends BaseDomain {
    private Long id;
    private Long userId;
    private String batchKey;
    private String groupId;
    private String cardCode;
    private String cardName;
    private String maskedCardNumber;
    private String cardBinType01;
    private String cardBinType02;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static QuickPaymentCard from(QuickPaymentCardCreateState state) {
        QuickPaymentCard card = QuickPaymentCard.builder()
                .userId(state.getUserId())
                .batchKey(state.getBatchKey())
                .groupId(state.getGroupId())
                .cardCode(state.getCardCode())
                .cardName(state.getCardName())
                .maskedCardNumber(state.getMaskedCardNumber())
                .cardBinType01(state.getCardBinType01())
                .cardBinType02(state.getCardBinType02())
                .build();
        card.activate();
        return card;
    }

    public static QuickPaymentCard from(QuickPaymentCardSnapshotState state) {
        QuickPaymentCard card = QuickPaymentCard.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .batchKey(state.getBatchKey())
                .groupId(state.getGroupId())
                .cardCode(state.getCardCode())
                .cardName(state.getCardName())
                .maskedCardNumber(state.getMaskedCardNumber())
                .cardBinType01(state.getCardBinType01())
                .cardBinType02(state.getCardBinType02())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        card.status = state.getStatus();
        return card;
    }
}
