package com.personal.marketnote.reward.exception;

import com.personal.marketnote.reward.domain.point.UserPointSourceType;

public class DuplicateUserPointHistoryException extends RuntimeException {
    public DuplicateUserPointHistoryException(Long userId, UserPointSourceType sourceType, Long sourceId, String reason) {
        super("이미 처리된 포인트 변경입니다. userId=" + userId
                + ", sourceType=" + sourceType
                + ", sourceId=" + sourceId
                + ", reason=" + reason);
    }
}
