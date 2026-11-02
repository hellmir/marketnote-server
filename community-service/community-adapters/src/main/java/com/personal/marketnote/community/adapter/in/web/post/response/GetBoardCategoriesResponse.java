package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.community.port.in.result.board.BoardCategoryItemResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardCategoriesResult;

import java.util.List;

public record GetBoardCategoriesResponse(
        List<BoardCategoryItemResult> categories
) {
    public static GetBoardCategoriesResponse from(GetBoardCategoriesResult getBoardCategoriesResult) {
        return new GetBoardCategoriesResponse(getBoardCategoriesResult.categories());
    }
}
