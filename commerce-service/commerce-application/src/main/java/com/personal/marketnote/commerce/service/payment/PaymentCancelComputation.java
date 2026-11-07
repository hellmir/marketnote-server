package com.personal.marketnote.commerce.service.payment;

import lombok.Builder;

/**
 * {@link PaymentCancelContext} 조립에 필요한 중간 계산 결과
 *
 * <p>{@code prepareCancel}에서 산출한 scalar 값 묶음을
 * {@link com.personal.marketnote.commerce.mapper.PaymentCancelContextMapper}로
 * 한 번에 전달하기 위한 내부 DTO.</p>
 */
@Builder
public record PaymentCancelComputation(
        Long cancelAmount,
        Long adjustedCancelAmount,
        Long remainAmount,
        Long alreadyRefunded,
        Long shippingFeeDeduction,
        boolean isFullCancel,
        String adjustedCancelReason,
        String cancelId,
        String transactionNumber,
        String modType
) {
    public static PaymentCancelComputation of(
            Long cancelAmount,
            Long adjustedCancelAmount,
            Long remainAmount,
            Long alreadyRefunded,
            Long shippingFeeDeduction,
            boolean isFullCancel,
            String adjustedCancelReason,
            String cancelId,
            String transactionNumber,
            String modType
    ) {
        return PaymentCancelComputation.builder()
                .cancelAmount(cancelAmount)
                .adjustedCancelAmount(adjustedCancelAmount)
                .remainAmount(remainAmount)
                .alreadyRefunded(alreadyRefunded)
                .shippingFeeDeduction(shippingFeeDeduction)
                .isFullCancel(isFullCancel)
                .adjustedCancelReason(adjustedCancelReason)
                .cancelId(cancelId)
                .transactionNumber(transactionNumber)
                .modType(modType)
                .build();
    }
}
