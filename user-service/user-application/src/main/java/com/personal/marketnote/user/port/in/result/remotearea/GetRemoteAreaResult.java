package com.personal.marketnote.user.port.in.result.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;

import java.util.List;
import java.util.stream.Collectors;

public record GetRemoteAreaResult(
        List<GetRemoteAreaItemResult> remoteAreas
) {
    public static GetRemoteAreaResult from(List<RemoteArea> remoteAreas) {
        return new GetRemoteAreaResult(
                remoteAreas.stream()
                        .map(GetRemoteAreaItemResult::from)
                        .collect(Collectors.toList())
        );
    }
}
