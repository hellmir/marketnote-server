package com.personal.marketnote.user.exception;

public class AdminSelfDeactivationException extends IllegalStateException {
    public AdminSelfDeactivationException(Long adminId) {
        super("ERR_USER_STATUS_02::관리자는 자신의 계정을 비활성화할 수 없습니다. adminId=" + adminId);
    }
}
