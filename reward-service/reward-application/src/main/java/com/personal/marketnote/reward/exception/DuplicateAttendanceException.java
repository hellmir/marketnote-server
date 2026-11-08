package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateAttendanceException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_ATTENDANCE_01::이미 출석이 등록된 날짜입니다.";

    public DuplicateAttendanceException() {
        super(MESSAGE);
    }
}
