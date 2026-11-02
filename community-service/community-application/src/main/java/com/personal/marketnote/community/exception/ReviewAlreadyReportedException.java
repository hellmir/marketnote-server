package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class ReviewAlreadyReportedException extends IllegalStateException {
    private static final String REVIEW_ALREADY_REPORTED_EXCEPTION_MESSAGE
            = "이미 해당 리뷰를 신고했습니다. 전송된 리뷰 ID: %d, 신고자 ID: %d";

    public ReviewAlreadyReportedException(Long id, Long reporterId) {
        super(String.format(REVIEW_ALREADY_REPORTED_EXCEPTION_MESSAGE, id, reporterId));
    }
}
