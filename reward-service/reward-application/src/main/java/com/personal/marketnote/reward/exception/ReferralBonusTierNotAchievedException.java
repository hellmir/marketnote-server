package com.personal.marketnote.reward.exception;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;

public class ReferralBonusTierNotAchievedException extends RuntimeException {
    public ReferralBonusTierNotAchievedException(ReferralBonusTier tier, long currentCount) {
        super("ERR_REFERRAL_01::보너스 티어가 달성되지 않았습니다. tier=" + tier
                + ", requiredCount=" + tier.getRequiredCount()
                + ", currentCount=" + currentCount);
    }
}
