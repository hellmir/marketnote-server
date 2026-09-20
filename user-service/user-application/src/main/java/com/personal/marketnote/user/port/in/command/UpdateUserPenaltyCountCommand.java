package com.personal.marketnote.user.port.in.command;

public record UpdateUserPenaltyCountCommand(Long userId, Long adminId, int penaltyCount, String reason) {
}
