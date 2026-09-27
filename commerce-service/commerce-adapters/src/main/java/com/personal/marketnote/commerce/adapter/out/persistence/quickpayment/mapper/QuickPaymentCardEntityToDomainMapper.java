package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity.QuickPaymentCardJpaEntity;
import com.personal.marketnote.commerce.domain.payment.MaskedCardNumber;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardSnapshotState;
import com.personal.marketnote.common.utility.FormatValidator;

public class QuickPaymentCardEntityToDomainMapper {

    public static QuickPaymentCard mapToDomain(QuickPaymentCardJpaEntity entity) {
        MaskedCardNumber maskedCardNumber = FormatValidator.hasValue(entity.getMaskedCardNumber())
                ? MaskedCardNumber.of(entity.getMaskedCardNumber())
                : null;
        return QuickPaymentCard.from(
                QuickPaymentCardSnapshotState.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .batchKey(entity.getBatchKey())
                        .groupId(entity.getGroupId())
                        .cardCode(entity.getCardCode())
                        .cardName(entity.getCardName())
                        .maskedCardNumber(maskedCardNumber)
                        .cardBinType01(entity.getCardBinType01())
                        .cardBinType02(entity.getCardBinType02())
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
