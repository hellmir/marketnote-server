package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ResizedFile {
    private Long id;
    private Long fileId;
    private String size;
    private String storageUrl;
    private LocalDateTime createdAt;
    private EntityStatus status;

    public static ResizedFile from(ResizedFileCreateState state) {
        return ResizedFile.builder()
                .fileId(state.getFileId())
                .size(state.getSize())
                .storageUrl(state.getStorageUrl())
                .build();
    }

    public static ResizedFile from(ResizedFileSnapshotState state) {
        return ResizedFile.builder()
                .id(state.getId())
                .fileId(state.getFileId())
                .size(state.getSize())
                .storageUrl(state.getStorageUrl())
                .createdAt(state.getCreatedAt())
                .status(state.getStatus())
                .build();
    }

    @Deprecated
    public static ResizedFile of(Long fileId, String size) {
        return ResizedFile.builder()
                .fileId(fileId)
                .size(size)
                .build();
    }

    @Deprecated
    public static ResizedFile of(Long fileId, String size, String storageUrl) {
        return ResizedFile.builder()
                .fileId(fileId)
                .size(size)
                .storageUrl(storageUrl)
                .build();
    }

    @Deprecated
    public static ResizedFile of(Long id, Long fileId, String size, LocalDateTime createdAt, EntityStatus status) {
        return ResizedFile.builder()
                .id(id)
                .fileId(fileId)
                .size(size)
                .createdAt(createdAt)
                .status(status)
                .build();
    }

    @Deprecated
    public static ResizedFile of(Long id, Long fileId, String size, String storageUrl, LocalDateTime createdAt, EntityStatus status) {
        return ResizedFile.builder()
                .id(id)
                .fileId(fileId)
                .size(size)
                .storageUrl(storageUrl)
                .createdAt(createdAt)
                .status(status)
                .build();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isInactive() {
        return status.isInactive();
    }

    public void delete() {
        status = EntityStatus.INACTIVE;
    }
}
