package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.EntityStatus;

public class InvalidUserStatusTransitionException extends IllegalStateException {
    public InvalidUserStatusTransitionException(EntityStatus currentStatus, String targetAction) {
        super("ERR_USER_STATUS_01::회원 상태 전이가 불가능합니다. 현재 상태: " + currentStatus.name() + ", 요청 액션: " + targetAction);
    }
}
