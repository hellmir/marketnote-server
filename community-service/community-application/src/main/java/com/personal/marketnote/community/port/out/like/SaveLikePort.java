package com.personal.marketnote.community.port.out.like;

import com.personal.marketnote.community.domain.like.Like;

/**
 * 좋아요 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 좋아요 저장 기능을 제공합니다.
 */
public interface SaveLikePort {
    /**
     * @param like 좋아요
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 좋아요를 저장합니다.
     */
    void save(Like like);
}
