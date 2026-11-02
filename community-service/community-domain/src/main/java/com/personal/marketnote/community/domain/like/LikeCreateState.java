package com.personal.marketnote.community.domain.like;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeCreateState {
    private final LikeTargetType targetType;
    private final Long targetId;
    private final Long userId;
}

