package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import lombok.Getter;

@Getter
public class LikeNotFoundException extends DomainNotFoundException {
    private static final String LIKE_NOT_FOUND_EXCEPTION_MESSAGE = "좋아요를 찾을 수 없습니다. 전송된 대상 유형: %s, 대상 ID: %d, 회원 ID: %d";

    public LikeNotFoundException(LikeTargetType targetType, Long targetId, Long userId) {
        super(String.format(LIKE_NOT_FOUND_EXCEPTION_MESSAGE, targetType, targetId, userId));
    }
}
