package com.personal.marketnote.reward.port.in.command.point;

public record RegisterUserPointCommand(Long userId, String userKey) {
    public static RegisterUserPointCommand of(Long userId, String userKey) {
        return new RegisterUserPointCommand(userId, userKey);
    }
}
