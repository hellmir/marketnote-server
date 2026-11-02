package com.personal.marketnote.user.exception;

public class UserWithdrawalCooldownException extends IllegalStateException {
    private static final String USER_WITHDRAWAL_COOLDOWN_EXCEPTION_MESSAGE =
            "%s:: 탈퇴 후 30일이 경과해야 재가입이 가능합니다.";

    public UserWithdrawalCooldownException(String code) {
        super(String.format(USER_WITHDRAWAL_COOLDOWN_EXCEPTION_MESSAGE, code));
    }
}
