package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;

public interface CheckReferralBonusClaimedPort {
    boolean isAlreadyClaimed(Long userId, ReferralBonusTier tier);
}
