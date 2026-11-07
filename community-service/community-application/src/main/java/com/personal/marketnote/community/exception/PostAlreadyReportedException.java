package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class PostAlreadyReportedException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 해당 게시글을 신고했습니다.";

    public PostAlreadyReportedException() {
        super(MESSAGE);
    }
}
