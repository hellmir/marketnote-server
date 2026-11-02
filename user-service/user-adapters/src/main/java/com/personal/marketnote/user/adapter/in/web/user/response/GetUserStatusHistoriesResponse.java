package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserStatusHistoryResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public record GetUserStatusHistoriesResponse(
        int pageSize,
        int pageNumber,
        long totalCount,
        boolean hasPrevious,
        boolean hasNext,
        List<GetUserStatusHistoryResponse> histories
) {
    public static GetUserStatusHistoriesResponse from(Page<GetUserStatusHistoryResult> page) {
        return new GetUserStatusHistoriesResponse(
                page.getPageable().getPageSize(),
                page.getPageable().getPageNumber() + 1,
                page.getTotalElements(),
                page.hasPrevious(),
                page.hasNext(),
                page.stream()
                        .map(GetUserStatusHistoryResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
