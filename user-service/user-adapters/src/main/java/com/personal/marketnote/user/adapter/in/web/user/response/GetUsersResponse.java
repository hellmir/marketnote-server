package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public record GetUsersResponse(
        int pageSize,
        int pageNumber,
        long totalCount,
        boolean hasPrevious,
        boolean hasNext,
        List<GetUserResponse> users
) {
    public static GetUsersResponse from(Page<GetUserResult> getUserResults) {
        return new GetUsersResponse(
                getUserResults.getPageable().getPageSize(),
                getUserResults.getPageable().getPageNumber() + 1,
                getUserResults.getTotalElements(),
                getUserResults.hasPrevious(),
                getUserResults.hasNext(),
                getUserResults.stream()
                        .map(GetUserResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
