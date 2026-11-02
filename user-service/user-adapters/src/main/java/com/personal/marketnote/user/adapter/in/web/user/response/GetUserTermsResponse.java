package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserTermsResult;

import java.util.List;
import java.util.stream.Collectors;

public record GetUserTermsResponse(
        List<UpdateUserTermResponse> userTerms
) {
    public static GetUserTermsResponse from(GetUserTermsResult getUserTermsResult) {
        return new GetUserTermsResponse(getUserTermsResult.userTerms().stream()
                .map(UpdateUserTermResponse::from)
                .collect(Collectors.toList()));
    }
}
