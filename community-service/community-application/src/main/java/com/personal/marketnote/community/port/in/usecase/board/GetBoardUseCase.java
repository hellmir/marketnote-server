package com.personal.marketnote.community.port.in.usecase.board;

import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.port.in.result.board.GetBoardCategoriesResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardsResult;

/**
 * 게시판 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-15
 * @Description 게시판 및 카테고리 조회 기능을 제공합니다.
 */
public interface GetBoardUseCase {
    /**
     * @param board 게시판
     * @return 게시글 카테고리 목록 조회 결과 {@link GetBoardCategoriesResult}
     * @Date 2026-01-15
     * @Author 성효빈
     * @Description 게시글 카테고리 목록을 조회합니다.
     */
    GetBoardCategoriesResult getCategories(Board board);

    /**
     * @return 게시판 목록 조회 결과 {@link GetBoardsResult}
     * @Date 2026-01-15
     * @Author 성효빈
     * @Description 게시판 목록을 조회합니다.
     */
    GetBoardsResult getBoards();
}
