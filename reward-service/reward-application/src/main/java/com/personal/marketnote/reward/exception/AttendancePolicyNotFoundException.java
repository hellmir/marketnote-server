package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AttendancePolicyNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "출석 정책을 찾을 수 없습니다. 전송된 id: %d";

    public AttendancePolicyNotFoundException(Short id) {
        super(String.format(MESSAGE, id));
    }
}
