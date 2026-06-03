package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.port.in.result.point.GetReferralStatusResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetReferralStatusUseCase;
import com.personal.marketnote.reward.port.out.point.CheckReferralBonusClaimedPort;
import com.personal.marketnote.reward.port.out.point.CountReferralPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetReferralStatusService implements GetReferralStatusUseCase {
    private final CountReferralPort countReferralPort;
    private final CheckReferralBonusClaimedPort checkReferralBonusClaimedPort;

    @Override
    public GetReferralStatusResult getReferralStatus(Long userId) {
        long totalInvitedCount = countReferralPort.countCompletedReferrals(userId);
        long totalEarnedCash = countReferralPort.sumReferralEarnedAmount(userId);

        Map<ReferralBonusTier, Boolean> claimedMap = Arrays.stream(ReferralBonusTier.values())
                .collect(Collectors.toMap(
                        tier -> tier,
                        tier -> checkReferralBonusClaimedPort.isAlreadyClaimed(userId, tier)
                ));

        return GetReferralStatusResult.of(totalInvitedCount, totalEarnedCash, claimedMap);
    }

    @Override
    public long countCompletedReferrals(Long userId) {
        return countReferralPort.countCompletedReferrals(userId);
    }
}
