package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class PostAlreadyReportedException extends IllegalStateException {
    private static final String POST_ALREADY_REPORTED_EXCEPTION_MESSAGE
            = "이미 해당 게시글을 신고했습니다. 전송된 게시글 ID: %d, 신고자 ID: %d";

    public PostAlreadyReportedException(Long id, Long reporterId) {
        super(String.format(POST_ALREADY_REPORTED_EXCEPTION_MESSAGE, id, reporterId));
    }
}
