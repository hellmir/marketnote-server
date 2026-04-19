package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResizedFileSnapshotState {
    private final Long id;
    private final Long fileId;
    private final String size;
    private final String storageUrl;
    private final LocalDateTime createdAt;
    private final EntityStatus status;
}
