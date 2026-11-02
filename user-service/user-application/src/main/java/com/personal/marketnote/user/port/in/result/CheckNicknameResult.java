package com.personal.marketnote.user.port.in.result;

public record CheckNicknameResult(
        boolean isDuplicated,
        boolean containsProfanity
) {
    public static CheckNicknameResult of(boolean isDuplicated, boolean containsProfanity) {
        return new CheckNicknameResult(isDuplicated, containsProfanity);
    }
}
