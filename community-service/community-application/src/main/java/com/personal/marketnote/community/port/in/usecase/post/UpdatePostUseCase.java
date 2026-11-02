package com.personal.marketnote.community.port.in.usecase.post;

import com.personal.marketnote.community.port.in.command.post.UpdatePostCommand;
import com.personal.marketnote.community.port.in.result.post.UpdatePostResult;

/**
 * 게시글 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-15
 * @Description 게시글 수정 기능을 제공합니다.
 */
public interface UpdatePostUseCase {
    /**
     * 게시글 수정
     *
     * @param command 게시글 수정 커맨드
     * @return 게시글 수정 결과 {@link UpdatePostResult}
     * @Author 성효빈
     * @Date 2026-01-15
     * @Description 게시글을 수정합니다.
     */
    UpdatePostResult updatePost(UpdatePostCommand command);
}
