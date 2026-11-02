package com.personal.marketnote.user.adapter.in.web.remotearea.response;

import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaResult;

import java.util.List;
import java.util.stream.Collectors;

public record GetRemoteAreaResponse(
        List<GetRemoteAreaItemResponse> remoteAreas
) {
    public static GetRemoteAreaResponse from(GetRemoteAreaResult result) {
        return new GetRemoteAreaResponse(
                result.remoteAreas().stream()
                        .map(GetRemoteAreaItemResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
