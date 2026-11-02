package com.personal.marketnote.reward.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReferralBonusTierTest {

    @Test
    @DisplayName("TIER_1은 5명 초대 시 1,000캐시 보너스이다")
    void tier1HasCorrectValues() {
        assertThat(ReferralBonusTier.TIER_1.getRequiredCount()).isEqualTo(5);
        assertThat(ReferralBonusTier.TIER_1.getBonusAmount()).isEqualTo(1_000);
        assertThat(ReferralBonusTier.TIER_1.getReason()).isEqualTo("친구 초대 누적 보너스 (5명)");
    }

    @Test
    @DisplayName("TIER_2는 10명 초대 시 1,500캐시 보너스이다")
    void tier2HasCorrectValues() {
        assertThat(ReferralBonusTier.TIER_2.getRequiredCount()).isEqualTo(10);
        assertThat(ReferralBonusTier.TIER_2.getBonusAmount()).isEqualTo(1_500);
        assertThat(ReferralBonusTier.TIER_2.getReason()).isEqualTo("친구 초대 누적 보너스 (10명)");
    }

    @Test
    @DisplayName("TIER_3은 20명 초대 시 2,000캐시 보너스이다")
    void tier3HasCorrectValues() {
        assertThat(ReferralBonusTier.TIER_3.getRequiredCount()).isEqualTo(20);
        assertThat(ReferralBonusTier.TIER_3.getBonusAmount()).isEqualTo(2_000);
        assertThat(ReferralBonusTier.TIER_3.getReason()).isEqualTo("친구 초대 누적 보너스 (20명)");
    }

    @ParameterizedTest
    @CsvSource({"4, false", "5, true", "6, true", "10, true"})
    @DisplayName("TIER_1은 초대 수가 5 이상이면 달성된다")
    void tier1IsAchievedWhenCountIsAtLeast5(long count, boolean expected) {
        assertThat(ReferralBonusTier.TIER_1.isAchieved(count)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"9, false", "10, true", "15, true", "20, true"})
    @DisplayName("TIER_2는 초대 수가 10 이상이면 달성된다")
    void tier2IsAchievedWhenCountIsAtLeast10(long count, boolean expected) {
        assertThat(ReferralBonusTier.TIER_2.isAchieved(count)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"19, false", "20, true", "25, true"})
    @DisplayName("TIER_3은 초대 수가 20 이상이면 달성된다")
    void tier3IsAchievedWhenCountIsAtLeast20(long count, boolean expected) {
        assertThat(ReferralBonusTier.TIER_3.isAchieved(count)).isEqualTo(expected);
    }

    @Test
    @DisplayName("초대 수가 정확히 5이면 TIER_1이 새로 달성된다")
    void findNewlyAchievedTierReturnsTier1WhenCount5() {
        Optional<ReferralBonusTier> tier = ReferralBonusTier.findNewlyAchievedTier(5);
        assertThat(tier).isPresent().contains(ReferralBonusTier.TIER_1);
    }

    @Test
    @DisplayName("초대 수가 정확히 10이면 TIER_2가 새로 달성된다")
    void findNewlyAchievedTierReturnsTier2WhenCount10() {
        Optional<ReferralBonusTier> tier = ReferralBonusTier.findNewlyAchievedTier(10);
        assertThat(tier).isPresent().contains(ReferralBonusTier.TIER_2);
    }

    @Test
    @DisplayName("초대 수가 정확히 20이면 TIER_3이 새로 달성된다")
    void findNewlyAchievedTierReturnsTier3WhenCount20() {
        Optional<ReferralBonusTier> tier = ReferralBonusTier.findNewlyAchievedTier(20);
        assertThat(tier).isPresent().contains(ReferralBonusTier.TIER_3);
    }

    @Test
    @DisplayName("초대 수가 단계에 해당하지 않으면 빈 결과를 반환한다")
    void findNewlyAchievedTierReturnsEmptyWhenNoTierMatch() {
        assertThat(ReferralBonusTier.findNewlyAchievedTier(7)).isEmpty();
        assertThat(ReferralBonusTier.findNewlyAchievedTier(1)).isEmpty();
        assertThat(ReferralBonusTier.findNewlyAchievedTier(15)).isEmpty();
    }

    @Test
    @DisplayName("초대 수가 10이면 TIER_1과 TIER_2가 달성된다")
    void findAllAchievedTiersReturns2TiersWhenCount10() {
        List<ReferralBonusTier> tiers = ReferralBonusTier.findAllAchievedTiers(10);
        assertThat(tiers).containsExactly(ReferralBonusTier.TIER_1, ReferralBonusTier.TIER_2);
    }

    @Test
    @DisplayName("초대 수가 20이면 모든 단계가 달성된다")
    void findAllAchievedTiersReturnsAllWhenCount20() {
        List<ReferralBonusTier> tiers = ReferralBonusTier.findAllAchievedTiers(20);
        assertThat(tiers).containsExactly(
                ReferralBonusTier.TIER_1, ReferralBonusTier.TIER_2, ReferralBonusTier.TIER_3
        );
    }

    @Test
    @DisplayName("초대 수가 3이면 달성된 단계가 없다")
    void findAllAchievedTiersReturnsEmptyWhenCount3() {
        assertThat(ReferralBonusTier.findAllAchievedTiers(3)).isEmpty();
    }

    @Test
    @DisplayName("초대 수가 20 이상이면 최대 초대 수 도달이다")
    void isMaxReachedReturnsTrueWhenCount20OrMore() {
        assertThat(ReferralBonusTier.isMaxReached(20)).isTrue();
        assertThat(ReferralBonusTier.isMaxReached(21)).isTrue();
    }

    @Test
    @DisplayName("초대 수가 19 이하이면 최대 초대 수 미도달이다")
    void isMaxReachedReturnsFalseWhenCountBelow20() {
        assertThat(ReferralBonusTier.isMaxReached(19)).isFalse();
        assertThat(ReferralBonusTier.isMaxReached(0)).isFalse();
    }

    @Test
    @DisplayName("최대 초대 가능 인원은 20이다")
    void getMaxInviteCountReturns20() {
        assertThat(ReferralBonusTier.getMaxInviteCount()).isEqualTo(20);
    }
}
