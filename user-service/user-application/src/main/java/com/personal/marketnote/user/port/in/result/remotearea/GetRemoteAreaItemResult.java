package com.personal.marketnote.user.port.in.result.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;

public record GetRemoteAreaItemResult(
        Long id,
        String province,
        String district,
        String village,
        String subarea
) {
    public static GetRemoteAreaItemResult from(RemoteArea remoteArea) {
        return new GetRemoteAreaItemResult(
                remoteArea.getId(),
                remoteArea.getProvince(),
                remoteArea.getDistrict(),
                remoteArea.getVillage(),
                remoteArea.getSubarea()
        );
    }
}
