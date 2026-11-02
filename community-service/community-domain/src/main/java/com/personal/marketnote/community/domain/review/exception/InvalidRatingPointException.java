package com.personal.marketnote.community.domain.review.exception;

public class InvalidRatingPointException extends IllegalArgumentException {
    private static final String INVALID_RATING_POINT_EXCEPTION_MESSAGE
            = "평점은 1 이상 5 이하의 정수만 가능합니다. 전송된 평점: %d";

    public InvalidRatingPointException(int point) {
        super(String.format(INVALID_RATING_POINT_EXCEPTION_MESSAGE, point));
    }
}
