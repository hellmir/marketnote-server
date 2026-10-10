package com.personal.marketnote.community.adapter.in.web.post.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.in.web.post.request.RegisterPostRequest;
import com.personal.marketnote.community.adapter.in.web.post.request.UpdatePostRequest;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.command.post.UpdatePostCommand;

public class PostRequestToCommandMapper {
    public static RegisterPostCommand mapToCommand(RegisterPostRequest request, Long userId) {
        return RegisterPostCommand.builder()
                .userId(userId)
                .parentId(request.getParentId())
                .board(request.getBoard())
                .category(request.getCategory())
                .targetGroupType(request.getTargetGroupType())
                .targetGroupId(request.getTargetGroupId())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .productImageUrl(request.getProductImageUrl())
                .writerName(request.getWriterName())
                .title(request.getTitle())
                .content(request.getContent())
                .isPrivate(request.getIsPrivate())
                .isPhoto(request.getIsPhoto())
                .isImportant(FormatValidator.hasValue(request.getIsImportant()) && request.getIsImportant())
                .build();
    }

    public static UpdatePostCommand mapToCommand(Long id, UpdatePostRequest request) {
        return UpdatePostCommand.of(id, request.getTitle(), request.getContent());
    }
}
