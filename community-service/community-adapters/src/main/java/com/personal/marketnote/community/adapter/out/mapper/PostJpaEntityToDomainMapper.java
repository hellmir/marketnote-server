package com.personal.marketnote.community.adapter.out.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;

import java.util.Optional;

public class PostJpaEntityToDomainMapper {
    public static Optional<Post> mapToDomain(PostJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }

        return Optional.of(
                Post.from(
                        PostSnapshotState.builder()
                                .id(entity.getId())
                                .userId(entity.getUserId())
                                .parentId(entity.getParentId())
                                .board(entity.getBoard())
                                .category(entity.getCategory())
                                .targetType(entity.getTargetType())
                                .targetId(entity.getTargetId())
                                .writerName(entity.getWriterName())
                                .writerMaskedName(entity.getWriterMaskedName())
                                .productImageUrl(entity.getProductImageUrl())
                                .title(entity.getTitle())
                                .content(entity.getContent())
                                .isPrivate(entity.isPrivate())
                                .isPhoto(entity.isPhoto())
                                .status(entity.getStatus())
                                .createdAt(entity.getCreatedAt())
                                .modifiedAt(entity.getModifiedAt())
                                .orderNum(entity.getOrderNum())
                                .build()
                )
        );
    }
}
