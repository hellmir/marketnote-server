package com.personal.marketnote.file.adapter.out.mapper;

import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;

import java.util.Optional;

public class FileJpaEntityToDomainMapper {
    public static Optional<FileDomain> mapToDomain(FileJpaEntity fileJpaEntity) {
        return Optional.ofNullable(fileJpaEntity)
                .map(entity -> FileDomain.from(
                        FileDomainSnapshotState.builder()
                                .id(entity.getId())
                                .ownerType(entity.getOwnerType())
                                .ownerId(entity.getOwnerId())
                                .sort(FileSort.from(entity.getSort()))
                                .extension(entity.getExtension())
                                .name(entity.getName())
                                .storageUrl(entity.getStorageUrl())
                                .createdAt(entity.getCreatedAt())
                                .status(entity.getStatus())
                                .orderNum(entity.getOrderNum())
                                .build()
                ));
    }
}
