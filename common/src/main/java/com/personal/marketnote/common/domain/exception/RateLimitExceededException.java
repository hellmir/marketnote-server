package com.personal.marketnote.common.domain.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String key, Long userId) {
        super("ERR_RATE_LIMIT_01::요청 횟수가 초과되었습니다. key=" + key + ", userId=" + userId);
    }
}
