package com.personal.marketnote.community.adapter.out.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeSnapshotState;

import java.util.Optional;

public class LikeJpaEntityToDomainMapper {
    public static Optional<Like> mapToDomain(LikeJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }

        return Optional.of(
                Like.from(
                        LikeSnapshotState.builder()
                                .targetType(entity.getId().getTargetType())
                                .targetId(entity.getId().getTargetId())
                                .userId(entity.getId().getUserId())
                                .status(entity.getStatus())
                                .createdAt(entity.getCreatedAt())
                                .modifiedAt(entity.getModifiedAt())
                                .build()
                )
        );
    }
}
