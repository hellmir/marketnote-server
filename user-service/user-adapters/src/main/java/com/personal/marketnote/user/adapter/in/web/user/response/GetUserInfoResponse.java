package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserInfoResult;

public record GetUserInfoResponse(
        GetUserInfoResult userInfo
) {
    public static GetUserInfoResponse from(GetUserInfoResult getUserInfoResult) {
        return new GetUserInfoResponse(getUserInfoResult);
    }
}
