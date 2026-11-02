package com.personal.marketnote.user.exception;

import lombok.Getter;

@Getter
public class MutualReferralNotAllowedException extends IllegalArgumentException {
    private static final String MUTUAL_REFERRAL_NOT_ALLOWED_EXCEPTION_MESSAGE
            = "%s:: 상호 추천은 허용되지 않습니다.";

    public MutualReferralNotAllowedException(String code) {
        super(String.format(MUTUAL_REFERRAL_NOT_ALLOWED_EXCEPTION_MESSAGE, code));
    }
}
