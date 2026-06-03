package com.personal.marketnote.reward.port.in.command.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;

public record ClaimReferralBonusCommand(
        Long userId,
        ReferralBonusTier tier
) {
}
