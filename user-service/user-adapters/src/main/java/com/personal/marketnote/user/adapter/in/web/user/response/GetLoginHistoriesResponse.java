package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetLoginHistoryResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public record GetLoginHistoriesResponse(
        int pageSize,
        int pageNumber,
        long totalCount,
        boolean hasPrevious,
        boolean hasNext,
        List<GetLoginHistoryResponse> histories
) {
    public static GetLoginHistoriesResponse from(Page<GetLoginHistoryResult> page) {
        return new GetLoginHistoriesResponse(
                page.getPageable().getPageSize(),
                page.getPageable().getPageNumber() + 1,
                page.getTotalElements(),
                page.hasPrevious(),
                page.hasNext(),
                page.stream()
                        .map(GetLoginHistoryResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
