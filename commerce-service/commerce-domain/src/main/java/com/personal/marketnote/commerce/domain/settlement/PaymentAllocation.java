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

        return PaymentAllocation.builder()
                .orderId(state.getOrderId())
                .sellerId(state.getSellerId())
                .allocatedAmount(state.getAllocatedAmount())
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
