package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateUserPointHistoryException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_POINT_HISTORY_03::이미 처리된 포인트 변경입니다.";

    public DuplicateUserPointHistoryException() {
        super(MESSAGE);
    }
}
