package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserPenaltyHistoryResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public record GetUserPenaltyHistoriesResponse(
        int pageSize,
        int pageNumber,
        long totalCount,
        boolean hasPrevious,
        boolean hasNext,
        List<GetUserPenaltyHistoryResponse> histories
) {
    public static GetUserPenaltyHistoriesResponse from(Page<GetUserPenaltyHistoryResult> page) {
        return new GetUserPenaltyHistoriesResponse(
                page.getPageable().getPageSize(),
                page.getPageable().getPageNumber() + 1,
                page.getTotalElements(),
                page.hasPrevious(),
                page.hasNext(),
                page.stream()
                        .map(GetUserPenaltyHistoryResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
