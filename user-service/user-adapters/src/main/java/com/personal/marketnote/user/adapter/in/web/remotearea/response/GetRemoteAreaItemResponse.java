package com.personal.marketnote.user.adapter.in.web.remotearea.response;

import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaItemResult;

public record GetRemoteAreaItemResponse(
        Long id,
        String province,
        String district,
        String village,
        String subarea
) {
    public static GetRemoteAreaItemResponse from(GetRemoteAreaItemResult result) {
        return new GetRemoteAreaItemResponse(
                result.id(),
                result.province(),
                result.district(),
                result.village(),
                result.subarea()
        );
    }
}
