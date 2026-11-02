package com.personal.marketnote.community.port.out.like;

import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.exception.LikeNotFoundException;

import java.util.Optional;

/**
 * 좋아요 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 좋아요 조회 관련 기능을 제공합니다.
 */
public interface FindLikePort {
    /**
     * @param targetType 좋아요 대상 타입
     * @param targetId   좋아요 대상 ID
     * @param userId     좋아요 대상 유형
     * @return 좋아요 대상 유형 존재 여부 {@link boolean}
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 좋아요 대상 유형 존재 여부를 조회합니다.
     */
    boolean existsByTargetAndUser(LikeTargetType targetType, Long targetId, Long userId);

    /**
     * @param targetType 좋아요 대상 타입
     * @param targetId   좋아요 대상 ID
     * @param userId     좋아요 대상 유형
     * @return 좋아요 대상 유형 {@link Like}
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 좋아요 대상 유형을 조회합니다.
     */
    Optional<Like> findByTargetAndUser(LikeTargetType targetType, Long targetId, Long userId)
            throws LikeNotFoundException;
}
