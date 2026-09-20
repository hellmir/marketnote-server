package com.personal.marketnote.user.port.in.command;

public record ApplyUserPenaltyCommand(Long userId, Long adminId, String reason) {
}
