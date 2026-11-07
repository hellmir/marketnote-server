package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

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

    public static ResizedFile of(Long fileId, String size) {
        return ResizedFile.from(
                ResizedFileCreateState.builder()
                        .fileId(fileId)
                        .size(size)
                        .build()
        );
    }

    public static ResizedFile withStorageUrl(ResizedFile base, String storageUrl) {
        return ResizedFile.from(
                ResizedFileCreateState.builder()
                        .fileId(base.getFileId())
                        .size(base.getSize())
                        .storageUrl(storageUrl)
                        .build()
        );
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
