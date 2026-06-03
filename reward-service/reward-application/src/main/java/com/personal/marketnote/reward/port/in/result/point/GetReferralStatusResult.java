package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            boolean achieved,
            boolean claimed,
            boolean claimable
    ) {
        public static BonusTierStatus of(ReferralBonusTier tier, boolean achieved, boolean claimed) {
            return BonusTierStatus.builder()
                    .requiredCount(tier.getRequiredCount())
                    .bonusAmount(tier.getBonusAmount())
                    .achieved(achieved)
                    .claimed(claimed)
                    .claimable(achieved && !claimed)
                    .build();
        }
    }

    public static GetReferralStatusResult of(long totalInvitedCount, long totalEarnedCash,
                                              Map<ReferralBonusTier, Boolean> claimedMap) {
        List<BonusTierStatus> tiers = Arrays.stream(ReferralBonusTier.values())
                .map(tier -> BonusTierStatus.of(
                        tier,
                        tier.isAchieved(totalInvitedCount),
                        claimedMap.getOrDefault(tier, false)
                ))
                .toList();

        return GetReferralStatusResult.builder()
                .totalInvitedCount(totalInvitedCount)
                .totalEarnedCash(totalEarnedCash)
                .maxInviteCount(ReferralBonusTier.getMaxInviteCount())
                .tiers(tiers)
                .build();
    }
}
