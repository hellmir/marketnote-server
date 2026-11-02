package com.personal.marketnote.community.port.in.usecase.like;

import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeTargetType;

/**
 * 좋아요 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 좋아요 조회 관련 기능을 제공합니다.
 */
public interface GetLikeUseCase {
    /**
     * @param targetType 좋아요 대상 타입
     * @param targetId   좋아요 대상 ID
     * @param userId     회원 ID
     * @return 회원 좋아요 존재 여부 {@link boolean}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 회원의 좋아요 존재 여부를 조회합니다.
     */
    boolean existsUserLike(LikeTargetType targetType, Long targetId, Long userId);

    /**
     * @param targetType 좋아요 대상 타입
     * @param targetId   좋아요 대상 ID
     * @param userId     회원 ID
     * @return 좋아요 {@link Like}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 회원의 좋아요를 조회합니다.
     */
    Like getLike(LikeTargetType targetType, Long targetId, Long userId);
}
