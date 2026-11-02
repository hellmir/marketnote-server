package com.personal.marketnote.community.port.in.usecase.post;

import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;

/**
 * 게시글 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 게시글 등록 기능을 제공합니다.
 */
public interface RegisterPostUseCase {
    /**
     * 게시글 등록
     *
     * @param isAdmin 관리자 여부
     * @param command 게시글 등록 커맨드
     * @return 게시글 등록 결과 {@link RegisterPostResult}
     * @Author 성효빈
     * @Date 2026-01-15
     * @Description 게시글을 등록합니다.
     */
    RegisterPostResult registerPost(boolean isAdmin, RegisterPostCommand command);
}
