package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class PostNotFoundException extends DomainNotFoundException {
    private static final String POST_NOT_FOUND_EXCEPTION_MESSAGE = "게시글을 찾을 수 없습니다. 전송된 게시글 ID: %d";

    public PostNotFoundException(Long id) {
        super(String.format(POST_NOT_FOUND_EXCEPTION_MESSAGE, id));
    }
}
