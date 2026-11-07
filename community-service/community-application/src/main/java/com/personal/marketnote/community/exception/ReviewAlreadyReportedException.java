package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class ReviewAlreadyReportedException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 해당 리뷰를 신고했습니다.";

    public ReviewAlreadyReportedException() {
        super(MESSAGE);
    }
}
