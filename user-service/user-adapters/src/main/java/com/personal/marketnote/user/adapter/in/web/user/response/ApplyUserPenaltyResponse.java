package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.ApplyUserPenaltyResult;

public record ApplyUserPenaltyResponse(Long userId, int penaltyCount, Long historyId) {
    public static ApplyUserPenaltyResponse from(ApplyUserPenaltyResult result) {
        return new ApplyUserPenaltyResponse(result.userId(), result.penaltyCount(), result.historyId());
    }
}
