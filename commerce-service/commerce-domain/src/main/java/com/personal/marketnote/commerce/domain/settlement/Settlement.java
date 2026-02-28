package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Settlement {
    private Long id;
    private Long sellerId;
    private Integer year;
    private Integer month;
    private Long totalAllocatedAmount;
    private Long pgFeeAmount;
    private Long platformFeeAmount;
    private Long sellerPayoutAmount;
    private SettlementStatus status;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Settlement from(SettlementCreateState state) {
        if (FormatValidator.hasNoValue(state.getTotalAllocatedAmount()) || state.getTotalAllocatedAmount() <= 0) {
            throw new IllegalArgumentException(
                    "총 배분 금액은 0보다 커야 합니다. totalAllocatedAmount=" + state.getTotalAllocatedAmount());
        }
        if (FormatValidator.hasNoValue(state.getSellerPayoutAmount()) || state.getSellerPayoutAmount() <= 0) {
            throw new IllegalArgumentException(
                    "판매자 지급액은 0보다 커야 합니다. sellerPayoutAmount=" + state.getSellerPayoutAmount());
        }
        long pgFee = state.getPgFeeAmount() != null ? state.getPgFeeAmount() : 0L;
        long platformFee = state.getPlatformFeeAmount() != null ? state.getPlatformFeeAmount() : 0L;

        if (pgFee < 0) {
            throw new IllegalArgumentException(
                    "PG 수수료는 음수일 수 없습니다. pgFeeAmount=" + pgFee);
        }
        if (platformFee < 0) {
            throw new IllegalArgumentException(
                    "플랫폼 수수료는 음수일 수 없습니다. platformFeeAmount=" + platformFee);
        }

        return Settlement.builder()
                .sellerId(state.getSellerId())
                .year(state.getYear())
                .month(state.getMonth())
                .totalAllocatedAmount(state.getTotalAllocatedAmount())
                .pgFeeAmount(pgFee)
                .platformFeeAmount(platformFee)
                .sellerPayoutAmount(state.getSellerPayoutAmount())
                .status(SettlementStatus.PENDING)
                .build();
    }

    public static Settlement from(SettlementSnapshotState state) {
        return Settlement.builder()
                .id(state.getId())
                .sellerId(state.getSellerId())
                .year(state.getYear())
                .month(state.getMonth())
                .totalAllocatedAmount(state.getTotalAllocatedAmount())
                .pgFeeAmount(state.getPgFeeAmount())
                .platformFeeAmount(state.getPlatformFeeAmount())
                .sellerPayoutAmount(state.getSellerPayoutAmount())
                .status(state.getStatus())
                .version(state.getVersion())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public boolean isPending() {
        return status == SettlementStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == SettlementStatus.COMPLETED;
    }

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
    }

    public void fail() {
        this.status = SettlementStatus.FAILED;
    }
}
