package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.port.in.result.point.GetReferralStatusResult;
import com.personal.marketnote.reward.port.out.point.CountReferralPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReferralStatusUseCaseTest {
    @InjectMocks
    private GetReferralStatusService getReferralStatusService;

    @Mock
    private CountReferralPort countReferralPort;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("초대 완료 건수가 7이면 TIER_1만 달성된 현황을 반환한다")
    void shouldReturnStatusWithTier1AchievedWhenCount7() {
        // given
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(7L);
        when(countReferralPort.sumReferralEarnedAmount(USER_ID)).thenReturn(4_500L);

        // when
        GetReferralStatusResult result = getReferralStatusService.getReferralStatus(USER_ID);

        // then
        assertThat(result.totalInvitedCount()).isEqualTo(7L);
        assertThat(result.totalEarnedCash()).isEqualTo(4_500L);
        assertThat(result.maxInviteCount()).isEqualTo(20);
        assertThat(result.tiers()).hasSize(3);
        assertThat(result.tiers().get(0).requiredCount()).isEqualTo(5);
        assertThat(result.tiers().get(0).achieved()).isTrue();
        assertThat(result.tiers().get(1).requiredCount()).isEqualTo(10);
        assertThat(result.tiers().get(1).achieved()).isFalse();
        assertThat(result.tiers().get(2).requiredCount()).isEqualTo(20);
        assertThat(result.tiers().get(2).achieved()).isFalse();

        verify(countReferralPort).countCompletedReferrals(USER_ID);
        verify(countReferralPort).sumReferralEarnedAmount(USER_ID);
    }

    @Test
    @DisplayName("초대 완료 건수가 0이면 달성된 단계가 없는 현황을 반환한다")
    void shouldReturnStatusWithNoTiersAchievedWhenCount0() {
        // given
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(0L);
        when(countReferralPort.sumReferralEarnedAmount(USER_ID)).thenReturn(0L);

        // when
        GetReferralStatusResult result = getReferralStatusService.getReferralStatus(USER_ID);

        // then
        assertThat(result.totalInvitedCount()).isEqualTo(0L);
        assertThat(result.totalEarnedCash()).isEqualTo(0L);
        assertThat(result.tiers()).hasSize(3);
        assertThat(result.tiers()).allSatisfy(tier -> assertThat(tier.achieved()).isFalse());
    }

    @Test
    @DisplayName("초대 완료 건수가 20이면 모든 단계가 달성된 현황을 반환한다")
    void shouldReturnStatusWithAllTiersAchievedWhenCount20() {
        // given
        long expectedTotalCash = 20L * 500L + 1_000L + 1_500L + 2_000L;
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(20L);
        when(countReferralPort.sumReferralEarnedAmount(USER_ID)).thenReturn(expectedTotalCash);

        // when
        GetReferralStatusResult result = getReferralStatusService.getReferralStatus(USER_ID);

        // then
        assertThat(result.totalInvitedCount()).isEqualTo(20L);
        assertThat(result.totalEarnedCash()).isEqualTo(expectedTotalCash);
        assertThat(result.tiers()).hasSize(3);
        assertThat(result.tiers()).allSatisfy(tier -> assertThat(tier.achieved()).isTrue());
    }

    @Test
    @DisplayName("보너스 단계별 금액이 올바르게 반환된다")
    void shouldReturnCorrectBonusAmountsForEachTier() {
        // given
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(10L);
        when(countReferralPort.sumReferralEarnedAmount(USER_ID)).thenReturn(6_500L);

        // when
        GetReferralStatusResult result = getReferralStatusService.getReferralStatus(USER_ID);

        // then
        assertThat(result.tiers().get(0).bonusAmount()).isEqualTo(ReferralBonusTier.TIER_1.getBonusAmount());
        assertThat(result.tiers().get(1).bonusAmount()).isEqualTo(ReferralBonusTier.TIER_2.getBonusAmount());
        assertThat(result.tiers().get(2).bonusAmount()).isEqualTo(ReferralBonusTier.TIER_3.getBonusAmount());
    }

    @Test
    @DisplayName("완료된 초대 건수를 조회한다")
    void shouldReturnCompletedReferralCount() {
        // given
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(12L);

        // when
        long count = getReferralStatusService.countCompletedReferrals(USER_ID);

        // then
        assertThat(count).isEqualTo(12L);
        verify(countReferralPort).countCompletedReferrals(USER_ID);
    }
}
