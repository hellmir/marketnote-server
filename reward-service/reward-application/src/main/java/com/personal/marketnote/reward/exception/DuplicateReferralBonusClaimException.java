package com.personal.marketnote.reward.exception;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;

public class DuplicateReferralBonusClaimException extends RuntimeException {
    public DuplicateReferralBonusClaimException(Long userId, ReferralBonusTier tier) {
        super("ERR_REFERRAL_02::이미 수령한 보너스입니다. userId=" + userId + ", tier=" + tier);
    }
}
