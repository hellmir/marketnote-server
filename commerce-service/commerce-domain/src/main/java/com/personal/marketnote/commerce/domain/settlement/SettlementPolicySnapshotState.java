package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class SettlementPolicySnapshotState {
    private Long id;
    private Long sellerId;
    private Integer pgFeeRate;
    private Integer platformFeeRate;
    private SettlementCycle settlementCycle;
    private Long minPayoutAmount;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
