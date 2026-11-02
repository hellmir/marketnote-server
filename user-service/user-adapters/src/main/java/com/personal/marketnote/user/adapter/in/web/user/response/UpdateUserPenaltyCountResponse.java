package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.UpdateUserPenaltyCountResult;

public record UpdateUserPenaltyCountResponse(Long userId, int penaltyCount, Long historyId) {
    public static UpdateUserPenaltyCountResponse from(UpdateUserPenaltyCountResult result) {
        return new UpdateUserPenaltyCountResponse(result.userId(), result.penaltyCount(), result.historyId());
    }
}
