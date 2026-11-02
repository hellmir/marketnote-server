package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetTermResult;

public record GetTermResponse(
        Long id,
        String content,
        Boolean isRequired
) {
    public static GetTermResponse from(GetTermResult getTermResult) {
        return new GetTermResponse(getTermResult.id(), getTermResult.content(), getTermResult.isRequired());
    }
}
