package com.personal.marketnote.user.port.in.result;

public record CheckNicknameResult(
        boolean isDuplicated
) {
    public static CheckNicknameResult of(boolean isDuplicated) {
        return new CheckNicknameResult(isDuplicated);
    }
}
