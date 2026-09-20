package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class SettlementPolicySnapshotState {
    private Long id;
    private Long sellerId;
    private FeeRate pgFeeRate;
    private FeeRate platformFeeRate;
    private SettlementCycle settlementCycle;
    private Long minPayoutAmount;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
