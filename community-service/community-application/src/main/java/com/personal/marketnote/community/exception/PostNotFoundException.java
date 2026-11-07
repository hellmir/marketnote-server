package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class PostNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "게시글을 찾을 수 없습니다.";

    public PostNotFoundException() {
        super(MESSAGE);
    }
}
