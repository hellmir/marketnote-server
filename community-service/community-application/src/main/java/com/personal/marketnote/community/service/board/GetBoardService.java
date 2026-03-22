package com.personal.marketnote.community.service.board;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.exception.InvalidBoardException;
import com.personal.marketnote.community.port.in.result.board.GetBoardCategoriesResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardsResult;
import com.personal.marketnote.community.port.in.usecase.board.GetBoardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetBoardService implements GetBoardUseCase {
    @Override
    public GetBoardCategoriesResult getCategories(Board board) {
        if (board.isNotice()) {
            return GetBoardCategoriesResult.from(NoticePostCategory.values());
        }

        if (board.isFaq()) {
            return GetBoardCategoriesResult.from(FaqPostCategory.values());
        }

        if (board.isProductInquery()) {
            return GetBoardCategoriesResult.from(ProductInqueryPostCategory.values());
        }

        if (board.isOneOnOneInquery()) {
            return GetBoardCategoriesResult.from(OneOnOneInqueryPostCategory.values());
        }

        throw new InvalidBoardException(board.name());
    }

    @Override
    public GetBoardsResult getBoards() {
        return GetBoardsResult.from(Board.values());
    }
}
