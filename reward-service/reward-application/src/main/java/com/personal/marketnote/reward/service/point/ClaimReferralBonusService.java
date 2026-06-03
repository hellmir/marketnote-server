package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateReferralBonusClaimException;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.exception.ReferralBonusTierNotAchievedException;
import com.personal.marketnote.reward.port.in.command.point.ClaimReferralBonusCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.ClaimReferralBonusResult;
import com.personal.marketnote.reward.port.in.usecase.point.ClaimReferralBonusUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.CheckReferralBonusClaimedPort;
import com.personal.marketnote.reward.port.out.point.CountReferralPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ClaimReferralBonusService implements ClaimReferralBonusUseCase {
    private final CountReferralPort countReferralPort;
    private final CheckReferralBonusClaimedPort checkReferralBonusClaimedPort;
    private final ModifyUserPointUseCase modifyUserPointUseCase;

    @Override
    public ClaimReferralBonusResult claim(ClaimReferralBonusCommand command) {
        Long userId = command.userId();
        ReferralBonusTier tier = command.tier();

        long referralCount = countReferralPort.countCompletedReferrals(userId);

        if (!tier.isAchieved(referralCount)) {
            throw new ReferralBonusTierNotAchievedException(tier, referralCount);
        }

        if (checkReferralBonusClaimedPort.isAlreadyClaimed(userId, tier)) {
            throw new DuplicateReferralBonusClaimException(userId, tier);
        }

        ModifyUserPointCommand modifyCommand = ModifyUserPointCommand.builder()
                .userId(userId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount((long) tier.getBonusAmount())
                .sourceType(UserPointSourceType.USER)
                .sourceId(userId)
                .reason(tier.getReason())
                .build();

        try {
            modifyUserPointUseCase.modify(modifyCommand);
        } catch (DuplicateUserPointHistoryException e) {
            throw new DuplicateReferralBonusClaimException(userId, tier);
        }

        return ClaimReferralBonusResult.from(tier);
    }
}
