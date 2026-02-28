package com.personal.marketnote.commerce.domain.settlement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SettlementSnapshotState {
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
}
