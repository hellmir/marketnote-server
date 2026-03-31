package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDomainSnapshotState {
    private final Long id;
    private final OwnerType ownerType;
    private final Long ownerId;
    private final FileSort sort;
    private final String extension;
    private final String name;
    private final String storageUrl;
    private final LocalDateTime createdAt;
    private final EntityStatus status;
    private final Long orderNum;
}

