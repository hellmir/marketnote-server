package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.reward.port.in.result.point.ClaimReferralBonusResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ClaimReferralBonusResponse(
        int requiredCount,
        int bonusAmount,
        String reason
) {
    public static ClaimReferralBonusResponse from(ClaimReferralBonusResult result) {
        return ClaimReferralBonusResponse.builder()
                .requiredCount(result.requiredCount())
                .bonusAmount(result.bonusAmount())
                .reason(result.reason())
                .build();
    }
}
