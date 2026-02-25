package com.personal.marketnote.community.port.out.like;

import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.exception.LikeNotFoundException;

/**
 * 좋아요 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 좋아요 수정 기능을 제공합니다.
 */
public interface UpdateLikePort {
    /**
     * @param like 좋아요
     * @throws LikeNotFoundException 좋아요 업데이트 실패 시 예외
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 좋아요를 업데이트합니다.
     */
    void update(Like like) throws LikeNotFoundException;
}
