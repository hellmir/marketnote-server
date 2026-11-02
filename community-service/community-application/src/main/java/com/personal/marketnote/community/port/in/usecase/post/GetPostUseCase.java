package com.personal.marketnote.community.port.in.usecase.post;

import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.port.in.command.post.GetPostQuery;
import com.personal.marketnote.community.port.in.command.post.GetPostsQuery;
import com.personal.marketnote.community.port.in.result.post.GetPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;

/**
 * 게시글 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 게시글 조회 관련 기능을 제공합니다.
 */
public interface GetPostUseCase {
    /**
     * @param id 게시글 ID
     * @return 게시글 존재 여부 {@link boolean}
     * @Date 2026-01-13
     * @Author 성효빈
     * @Description 게시글 존재 여부를 조회합니다.
     */
    boolean existsPost(Long id);

    /**
     * @param query 게시글 조회 쿼리
     * @return 게시글 목록 조회 결과 {@link GetPostsResult}
     * @Date 2025-12-06
     * @Author 성효빈
     * @Description 게시글 목록을 조회합니다.
     */
    GetPostsResult getPosts(GetPostsQuery query);

    /**
     * @param query 게시글 조회 쿼리
     * @return 게시글 조회 결과 {@link PostItemResult}
     * @Date 2026-01-27
     * @Author 성효빈
     * @Description 게시글 정보를 조회합니다.
     */
    PostItemResult getPost(GetPostQuery query);

    /**
     * @param id 게시글 ID
     * @return 게시글 {@link Post}
     * @Date 2026-01-15
     * @Author 성효빈
     * @Description 게시글을 조회합니다.
     */
    Post getPost(Long id);
}
