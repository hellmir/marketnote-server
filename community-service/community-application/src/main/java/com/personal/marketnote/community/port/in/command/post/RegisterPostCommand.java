package com.personal.marketnote.community.port.in.command.post;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import com.personal.marketnote.community.domain.post.PostTargetType;
import lombok.Builder;

@Builder
public record RegisterPostCommand(
        Long userId,
        Long parentId,
        Board board,
        String category,
        PostTargetGroupType targetGroupType,
        Long targetGroupId,
        PostTargetType targetType,
        Long targetId,
        String productImageUrl,
        String writerName,
        String title,
        String content,
        boolean isPrivate,
        boolean isPhoto,
        boolean isImportant
) {
    public boolean isReply() {
        return FormatValidator.hasValue(parentId);
    }
}
