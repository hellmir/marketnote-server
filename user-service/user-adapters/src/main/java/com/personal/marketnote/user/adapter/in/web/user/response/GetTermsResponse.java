package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetTermsResult;

import java.util.List;
import java.util.stream.Collectors;

public record GetTermsResponse(
        List<GetTermResponse> terms
) {
    public static GetTermsResponse from(GetTermsResult getTermsResult) {
        return new GetTermsResponse(getTermsResult.getTermResults().stream()
                .map(GetTermResponse::from)
                .collect(Collectors.toList()));
    }
}
