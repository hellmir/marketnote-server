package com.personal.marketnote.user.domain.remotearea;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteAreaSnapshotState {
    private final Long id;
    private final String zipCode;
    private final RemoteAreaType remoteAreaType;
    private final String regionName;
}
