package com.personal.marketnote.community.adapter.in.web.like.mapper;

import com.personal.marketnote.community.adapter.in.web.like.request.UpsertLikeRequest;
import com.personal.marketnote.community.port.in.command.like.UpsertLikeCommand;

public class LikeRequestToCommandMapper {
    public static UpsertLikeCommand mapToCommand(UpsertLikeRequest request, Long userId) {
        return UpsertLikeCommand.of(request.targetType(), request.targetId(), request.isLiked(), userId);
    }
}
