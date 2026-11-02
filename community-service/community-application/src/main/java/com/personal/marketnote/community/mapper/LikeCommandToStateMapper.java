package com.personal.marketnote.community.mapper;

import com.personal.marketnote.community.domain.like.LikeCreateState;
import com.personal.marketnote.community.port.in.command.like.UpsertLikeCommand;

public class LikeCommandToStateMapper {
    private LikeCommandToStateMapper() {
    }

    public static LikeCreateState mapToState(UpsertLikeCommand command) {
        return LikeCreateState.builder()
                .targetType(command.targetType())
                .targetId(command.targetId())
                .userId(command.userId())
                .build();
    }
}
