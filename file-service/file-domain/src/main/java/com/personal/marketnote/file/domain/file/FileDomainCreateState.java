package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDomainCreateState {
    private final OwnerType ownerType;
    private final Long ownerId;
    private final FileSort sort;
    private final String extension;
    private final String name;
    private final Long userId;
    private final String ownerKey;
}

