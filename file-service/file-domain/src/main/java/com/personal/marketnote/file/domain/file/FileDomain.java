package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.domain.file.exception.InvalidFileOwnerException;
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
    private Long userId;
    private String ownerKey;

    public static FileDomain from(FileDomainCreateState state) {
        if (FormatValidator.hasNoValue(state.getUserId()) || FormatValidator.hasNoValue(state.getOwnerKey())) {
            throw new InvalidFileOwnerException();
        }
        return FileDomain.builder()
                .ownerType(state.getOwnerType())
                .ownerId(state.getOwnerId())
                .sort(state.getSort())
                .extension(state.getExtension())
                .name(state.getName())
                .userId(state.getUserId())
                .ownerKey(state.getOwnerKey())
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
                .userId(state.getUserId())
                .ownerKey(state.getOwnerKey())
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

    public void validateOwner(Long requesterId, String ownerKey) {
        if (FormatValidator.hasNoValue(userId) || !userId.equals(requesterId)) {
            throw new InvalidFileOwnerException();
        }
        if (FormatValidator.hasNoValue(this.ownerKey) || !this.ownerKey.equals(ownerKey)) {
            throw new InvalidFileOwnerException();
        }
    }
}
