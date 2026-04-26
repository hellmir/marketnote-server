package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PaymentAllocation {
    private Long id;
    private Long orderId;
    private Long sellerId;
    private Long allocatedAmount;
    private Long shippingFee;
    private Long settlementId;
    private PaymentAllocationTransactionType transactionType;
    private PaymentAllocationTargetType targetType;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public static PaymentAllocation from(PaymentAllocationCreateState state) {
        if (FormatValidator.hasNoValue(state.getAllocatedAmount()) || state.getAllocatedAmount() <= 0) {
            throw new InvalidSettlementAmountException(
                    "배분 금액은 0보다 커야 합니다. allocatedAmount=" + state.getAllocatedAmount());
        }

        Long shippingFee = FormatValidator.hasValue(state.getShippingFee()) ? state.getShippingFee() : 0L;
        if (shippingFee < 0) {
            throw new InvalidSettlementAmountException(
                    "배송비는 음수일 수 없습니다. shippingFee=" + shippingFee);
        }

        return PaymentAllocation.builder()
                .orderId(state.getOrderId())
                .sellerId(state.getSellerId())
                .allocatedAmount(state.getAllocatedAmount())
                .shippingFee(shippingFee)
                .transactionType(state.getTransactionType())
                .targetType(state.getTargetType())
                .idempotencyKey(state.getIdempotencyKey())
                .build();
    }

    public static PaymentAllocation from(PaymentAllocationSnapshotState state) {
        return PaymentAllocation.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .sellerId(state.getSellerId())
                .allocatedAmount(state.getAllocatedAmount())
                .shippingFee(state.getShippingFee())
                .settlementId(state.getSettlementId())
                .transactionType(state.getTransactionType())
                .targetType(state.getTargetType())
                .idempotencyKey(state.getIdempotencyKey())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public boolean isSettled() {
        return FormatValidator.hasValue(settlementId);
    }

    public void assignSettlement(Long settlementId) {
        this.settlementId = settlementId;
    }
}
