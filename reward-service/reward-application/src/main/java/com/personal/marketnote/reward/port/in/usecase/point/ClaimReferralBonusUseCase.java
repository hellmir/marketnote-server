package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.ClaimReferralBonusCommand;
import com.personal.marketnote.reward.port.in.result.point.ClaimReferralBonusResult;

public interface ClaimReferralBonusUseCase {
    ClaimReferralBonusResult claim(ClaimReferralBonusCommand command);
}
