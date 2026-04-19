package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class ReviewNotFoundException extends DomainNotFoundException {
    private static final String REVIEW_NOT_FOUND_EXCEPTION_MESSAGE = "리뷰를 찾을 수 없습니다. 전송된 리뷰 ID: %d";

    public ReviewNotFoundException(Long id) {
        super(String.format(REVIEW_NOT_FOUND_EXCEPTION_MESSAGE, id));
    }
}
