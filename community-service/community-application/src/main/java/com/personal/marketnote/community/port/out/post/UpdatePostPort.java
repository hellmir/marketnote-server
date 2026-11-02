package com.personal.marketnote.community.port.out.post;

import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.exception.PostNotFoundException;

/**
 * 게시글 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-15
 * @Description 게시글 수정 기능을 제공합니다.
 */
public interface UpdatePostPort {
    /**
     * @param post 게시글
     * @throws PostNotFoundException 게시글 수정 실패 시 예외
     * @Date 2026-01-15
     * @Author 성효빈
     * @Description 게시글을 수정합니다.
     */
    void update(Post post) throws PostNotFoundException;
}
