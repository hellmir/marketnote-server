package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateReferralBonusClaimException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_REFERRAL_02::이미 수령한 보너스입니다.";

    public DuplicateReferralBonusClaimException() {
        super(MESSAGE);
    }
}
