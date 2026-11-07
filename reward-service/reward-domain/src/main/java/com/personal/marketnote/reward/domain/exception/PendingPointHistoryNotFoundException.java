package com.personal.marketnote.reward.domain.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class PendingPointHistoryNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "확정 대상 적립 예정 포인트 이력을 찾을 수 없습니다.";

    public PendingPointHistoryNotFoundException() {
        super(MESSAGE);
    }
}
