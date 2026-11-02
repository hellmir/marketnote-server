package com.personal.marketnote.community.port.in.usecase.like;

import com.personal.marketnote.community.port.in.command.like.UpsertLikeCommand;
import com.personal.marketnote.community.port.in.result.like.UpsertLikeResult;

/**
 * 좋아요 등록/취소 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 좋아요 등록 및 취소 기능을 제공합니다.
 */
public interface UpsertLikeUseCase {
    /**
     * @param command 좋아요 등록/취소 커맨드
     * @return 좋아요 등록/취소 결과 {@link UpsertLikeResult}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 좋아요를 등록하거나 취소합니다.
     */
    UpsertLikeResult upsertLike(UpsertLikeCommand command);
}
