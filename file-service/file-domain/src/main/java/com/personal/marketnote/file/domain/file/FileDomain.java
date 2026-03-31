package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FileDomain {
    private Long id;
    private OwnerType ownerType;
    private Long ownerId;
    private FileSort sort;
    private String extension;
    private String name;
    private String storageUrl;
    private LocalDateTime createdAt;
    private EntityStatus status;
    private Long orderNum;

    public static FileDomain from(FileDomainCreateState state) {
        return FileDomain.builder()
                .ownerType(state.getOwnerType())
                .ownerId(state.getOwnerId())
                .sort(state.getSort())
                .extension(state.getExtension())
                .name(state.getName())
                .build();
    }

    public static FileDomain from(FileDomainSnapshotState state) {
        return FileDomain.builder()
                .id(state.getId())
                .ownerType(state.getOwnerType())
                .ownerId(state.getOwnerId())
                .sort(state.getSort())
                .extension(state.getExtension())
                .name(state.getName())
                .storageUrl(state.getStorageUrl())
                .createdAt(state.getCreatedAt())
                .status(state.getStatus())
                .orderNum(state.getOrderNum())
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
