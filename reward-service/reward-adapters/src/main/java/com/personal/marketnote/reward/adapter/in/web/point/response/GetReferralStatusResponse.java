package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.reward.port.in.result.point.GetReferralStatusResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetReferralStatusResponse(
        long totalInvitedCount,
        long totalEarnedCash,
        int maxInviteCount,
        List<BonusTierResponse> tiers
) {
    @Builder(access = AccessLevel.PRIVATE)
    public record BonusTierResponse(
            int requiredCount,
            int bonusAmount,
            boolean achieved
    ) {
        public static BonusTierResponse from(GetReferralStatusResult.BonusTierStatus status) {
            return BonusTierResponse.builder()
                    .requiredCount(status.requiredCount())
                    .bonusAmount(status.bonusAmount())
                    .achieved(status.achieved())
                    .build();
        }
    }

    public static GetReferralStatusResponse from(GetReferralStatusResult result) {
        return GetReferralStatusResponse.builder()
                .totalInvitedCount(result.totalInvitedCount())
                .totalEarnedCash(result.totalEarnedCash())
                .maxInviteCount(result.maxInviteCount())
                .tiers(result.tiers().stream()
                        .map(BonusTierResponse::from)
                        .toList())
                .build();
    }
}
