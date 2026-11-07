package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class UserPointNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "회원 포인트 정보를 찾을 수 없습니다.";

    public UserPointNotFoundException() {
        super(MESSAGE);
    }
}
