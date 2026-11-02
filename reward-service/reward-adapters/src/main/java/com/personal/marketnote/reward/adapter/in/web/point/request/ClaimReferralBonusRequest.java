package com.personal.marketnote.reward.adapter.in.web.point.request;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import jakarta.validation.constraints.NotNull;

public record ClaimReferralBonusRequest(
        @NotNull(message = "보너스 티어는 필수입니다")
        ReferralBonusTier tier
) {
}
