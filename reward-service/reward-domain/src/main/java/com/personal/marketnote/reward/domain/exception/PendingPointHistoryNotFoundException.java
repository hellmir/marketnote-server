package com.personal.marketnote.reward.domain.exception;

public class PendingPointHistoryNotFoundException extends RuntimeException {
    private static final String MESSAGE = "확정 대상 적립 예정 포인트 이력을 찾을 수 없습니다. userId=%d, sourceType=%s, sourceId=%d";

    public PendingPointHistoryNotFoundException(Long userId, String sourceType, Long sourceId) {
        super(String.format(MESSAGE, userId, sourceType, sourceId));
    }
}
