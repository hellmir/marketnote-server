package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;

import java.util.List;

public record GetUserPointHistoryResponse(
        CursorResponse<UserPointHistoryByDateResponse> histories
) {
    public static GetUserPointHistoryResponse from(GetUserPointHistoryResult result) {
        List<UserPointHistoryByDateResponse> items = result.histories().stream()
                .map(UserPointHistoryByDateResponse::from)
                .toList();

        return new GetUserPointHistoryResponse(
                new CursorResponse<>(
                        result.totalElements(),
                        result.hasNext(),
                        result.nextCursor(),
                        items
                )
        );
    }
}
