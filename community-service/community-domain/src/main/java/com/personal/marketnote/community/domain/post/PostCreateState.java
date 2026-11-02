package com.personal.marketnote.community.domain.post;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCreateState {
    private final Long userId;
    private final Long parentId;
    private final Board board;
    private final String category;
    private final PostTargetGroupType targetGroupType;
    private final Long targetGroupId;
    private final PostTargetType targetType;
    private final Long targetId;
    private final String productImageUrl;
    private final String writerName;
    private final String title;
    private final String content;
    private final boolean isPrivate;
    private final boolean isPhoto;
    private final boolean isImportant;
}
