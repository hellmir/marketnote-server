package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateOfferwallRewardException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 해당 캠페인에 대한 리워드가 지급되었습니다.";

    public DuplicateOfferwallRewardException() {
        super(MESSAGE);
    }
}
