package com.personal.marketnote.reward.domain.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ReferralBonusTier {
    TIER_1(5, 1_000, "친구 초대 누적 보너스 (5명)"),
    TIER_2(10, 1_500, "친구 초대 누적 보너스 (10명)"),
    TIER_3(20, 2_000, "친구 초대 누적 보너스 (20명)");

    private static final int MAX_INVITE_COUNT = TIER_3.requiredCount;

    private final int requiredCount;
    private final int bonusAmount;
    private final String reason;

    public boolean isAchieved(long count) {
        return count >= requiredCount;
    }

    public static Optional<ReferralBonusTier> findNewlyAchievedTier(long count) {
        return Arrays.stream(values())
                .filter(tier -> tier.requiredCount == count)
                .findFirst();
    }

    public static List<ReferralBonusTier> findAllAchievedTiers(long count) {
        return Arrays.stream(values())
                .filter(tier -> tier.isAchieved(count))
                .toList();
    }

    public static boolean isMaxReached(long count) {
        return count >= MAX_INVITE_COUNT;
    }

    public static int getMaxInviteCount() {
        return MAX_INVITE_COUNT;
    }
}
