package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateReferralBonusClaimException;
import com.personal.marketnote.reward.exception.ReferralBonusTierNotAchievedException;
import com.personal.marketnote.reward.port.in.command.point.ClaimReferralBonusCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.ClaimReferralBonusResult;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.CheckReferralBonusClaimedPort;
import com.personal.marketnote.reward.port.out.point.CountReferralPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimReferralBonusUseCaseTest {
    @InjectMocks
    private ClaimReferralBonusService claimReferralBonusService;

    @Mock
    private CountReferralPort countReferralPort;

    @Mock
    private CheckReferralBonusClaimedPort checkReferralBonusClaimedPort;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("5명 티어 달성 시 보너스 클레임 요청에 성공한다")
    void shouldClaimTier1BonusWhenCount5Achieved() {
        // given
        ClaimReferralBonusCommand command = new ClaimReferralBonusCommand(USER_ID, ReferralBonusTier.TIER_1);
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(5L);
        when(checkReferralBonusClaimedPort.isAlreadyClaimed(USER_ID, ReferralBonusTier.TIER_1)).thenReturn(false);

        // when
        ClaimReferralBonusResult result = claimReferralBonusService.claim(command);

        // then
        assertThat(result.requiredCount()).isEqualTo(5);
        assertThat(result.bonusAmount()).isEqualTo(1_000);
        assertThat(result.reason()).isEqualTo(ReferralBonusTier.TIER_1.getReason());

        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(captor.capture());

        ModifyUserPointCommand modifyCommand = captor.getValue();
        assertThat(modifyCommand.userId()).isEqualTo(USER_ID);
        assertThat(modifyCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(modifyCommand.amount()).isEqualTo(1_000L);
        assertThat(modifyCommand.sourceType()).isEqualTo(UserPointSourceType.USER);
        assertThat(modifyCommand.sourceId()).isEqualTo(USER_ID);
        assertThat(modifyCommand.reason()).isEqualTo(ReferralBonusTier.TIER_1.getReason());
    }

    @Test
    @DisplayName("10명 티어 달성 시 보너스 클레임 요청에 성공한다")
    void shouldClaimTier2BonusWhenCount10Achieved() {
        // given
        ClaimReferralBonusCommand command = new ClaimReferralBonusCommand(USER_ID, ReferralBonusTier.TIER_2);
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(12L);
        when(checkReferralBonusClaimedPort.isAlreadyClaimed(USER_ID, ReferralBonusTier.TIER_2)).thenReturn(false);

        // when
        ClaimReferralBonusResult result = claimReferralBonusService.claim(command);

        // then
        assertThat(result.requiredCount()).isEqualTo(10);
        assertThat(result.bonusAmount()).isEqualTo(1_500);
        assertThat(result.reason()).isEqualTo(ReferralBonusTier.TIER_2.getReason());

        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
    }

    @Test
    @DisplayName("20명 티어 달성 시 보너스 클레임 요청에 성공한다")
    void shouldClaimTier3BonusWhenCount20Achieved() {
        // given
        ClaimReferralBonusCommand command = new ClaimReferralBonusCommand(USER_ID, ReferralBonusTier.TIER_3);
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(20L);
        when(checkReferralBonusClaimedPort.isAlreadyClaimed(USER_ID, ReferralBonusTier.TIER_3)).thenReturn(false);

        // when
        ClaimReferralBonusResult result = claimReferralBonusService.claim(command);

        // then
        assertThat(result.requiredCount()).isEqualTo(20);
        assertThat(result.bonusAmount()).isEqualTo(2_000);
        assertThat(result.reason()).isEqualTo(ReferralBonusTier.TIER_3.getReason());

        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(captor.capture());

        ModifyUserPointCommand modifyCommand = captor.getValue();
        assertThat(modifyCommand.amount()).isEqualTo(2_000L);
    }

    @Test
    @DisplayName("티어 미달성 시 보너스 클레임 요청에 실패한다")
    void shouldThrowExceptionWhenTierNotAchieved() {
        // given
        ClaimReferralBonusCommand command = new ClaimReferralBonusCommand(USER_ID, ReferralBonusTier.TIER_2);
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(7L);

        // expect
        assertThatThrownBy(() -> claimReferralBonusService.claim(command))
                .isInstanceOf(ReferralBonusTierNotAchievedException.class);

        verifyNoInteractions(checkReferralBonusClaimedPort);
        verifyNoInteractions(modifyUserPointUseCase);
    }

    @Test
    @DisplayName("이미 수령한 보너스를 중복 클레임하면 실패한다")
    void shouldThrowExceptionWhenAlreadyClaimed() {
        // given
        ClaimReferralBonusCommand command = new ClaimReferralBonusCommand(USER_ID, ReferralBonusTier.TIER_1);
        when(countReferralPort.countCompletedReferrals(USER_ID)).thenReturn(5L);
        when(checkReferralBonusClaimedPort.isAlreadyClaimed(USER_ID, ReferralBonusTier.TIER_1)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> claimReferralBonusService.claim(command))
                .isInstanceOf(DuplicateReferralBonusClaimException.class);

        verifyNoInteractions(modifyUserPointUseCase);
    }
}
