package com.personal.marketnote.user.domain.remotearea;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteAreaCreateState {
    private final String province;
    private final String district;
    private final String village;
    private final String subarea;
}
