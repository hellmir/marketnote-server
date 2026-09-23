package com.personal.marketnote.community.port.in.usecase.post;

import com.personal.marketnote.community.port.in.result.post.GetPostKeyResult;

/**
 * 게시글 postKey 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 게시글의 postKey를 조회합니다. 작성자 본인만 조회할 수 있습니다.
 */
public interface GetPostKeyUseCase {
    /**
     * @param postId 게시글 ID
     * @param userId 요청자 회원 ID
     * @return postKey 조회 결과 {@link GetPostKeyResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 게시글의 postKey를 조회합니다. 작성자 본인만 조회 가능합니다.
     */
    GetPostKeyResult getPostKey(Long postId, Long userId);
}
