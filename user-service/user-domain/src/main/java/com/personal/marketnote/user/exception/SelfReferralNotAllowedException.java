package com.personal.marketnote.user.exception;

import lombok.Getter;

@Getter
public class SelfReferralNotAllowedException extends IllegalArgumentException {
    private static final String SELF_REFERRAL_NOT_ALLOWED_EXCEPTION_MESSAGE
            = "%s:: 자기 자신의 추천 회원 초대 코드는 등록할 수 없습니다.";

    public SelfReferralNotAllowedException(String code) {
        super(String.format(SELF_REFERRAL_NOT_ALLOWED_EXCEPTION_MESSAGE, code));
    }
}
