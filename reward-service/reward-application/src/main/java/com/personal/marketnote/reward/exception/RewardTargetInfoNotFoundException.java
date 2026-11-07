package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class RewardTargetInfoNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "오퍼월 리워드 지급 대상 디바이스 정보가 없습니다.";

    public RewardTargetInfoNotFoundException() {
        super(MESSAGE);
    }
}
