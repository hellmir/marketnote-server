package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity.QuickPaymentCardJpaEntity;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardSnapshotState;

public class QuickPaymentCardEntityToDomainMapper {

    public static QuickPaymentCard mapToDomain(QuickPaymentCardJpaEntity entity) {
        return QuickPaymentCard.from(
                QuickPaymentCardSnapshotState.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .batchKey(entity.getBatchKey())
                        .groupId(entity.getGroupId())
                        .cardCode(entity.getCardCode())
                        .cardName(entity.getCardName())
                        .maskedCardNumber(entity.getMaskedCardNumber())
                        .cardBinType01(entity.getCardBinType01())
                        .cardBinType02(entity.getCardBinType02())
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
