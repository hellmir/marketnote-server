package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;

public record ClaimReferralBonusResult(
        int requiredCount,
        int bonusAmount,
        String reason
) {
    public static ClaimReferralBonusResult from(ReferralBonusTier tier) {
        return new ClaimReferralBonusResult(
                tier.getRequiredCount(),
                tier.getBonusAmount(),
                tier.getReason()
        );
    }
}
