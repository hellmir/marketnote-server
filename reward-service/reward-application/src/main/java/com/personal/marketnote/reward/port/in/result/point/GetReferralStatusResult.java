package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.Arrays;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetReferralStatusResult(
        long totalInvitedCount,
        long totalEarnedCash,
        int maxInviteCount,
        List<BonusTierStatus> tiers
) {
    @Builder(access = AccessLevel.PRIVATE)
    public record BonusTierStatus(
            int requiredCount,
            int bonusAmount,
            boolean achieved
    ) {
        public static BonusTierStatus of(ReferralBonusTier tier, boolean achieved) {
            return BonusTierStatus.builder()
                    .requiredCount(tier.getRequiredCount())
                    .bonusAmount(tier.getBonusAmount())
                    .achieved(achieved)
                    .build();
        }
    }

    public static GetReferralStatusResult of(long totalInvitedCount, long totalEarnedCash) {
        List<BonusTierStatus> tiers = Arrays.stream(ReferralBonusTier.values())
                .map(tier -> BonusTierStatus.of(tier, tier.isAchieved(totalInvitedCount)))
                .toList();

        return GetReferralStatusResult.builder()
                .totalInvitedCount(totalInvitedCount)
                .totalEarnedCash(totalEarnedCash)
                .maxInviteCount(ReferralBonusTier.getMaxInviteCount())
                .tiers(tiers)
                .build();
    }
}
