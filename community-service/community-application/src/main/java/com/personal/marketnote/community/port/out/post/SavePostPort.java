package com.personal.marketnote.community.port.out.post;

import com.personal.marketnote.community.domain.post.Post;

/**
 * 게시글 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 게시글 저장 기능을 제공합니다.
 */
public interface SavePostPort {
    /**
     * @param post 게시글
     * @return 저장된 게시글 {@link Post}
     * @Date 2026-01-13
     * @Author 성효빈
     * @Description 게시글을 저장합니다.
     */
    Post save(Post post);
}
