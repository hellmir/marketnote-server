package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.community.port.in.result.board.BoardItemResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardsResult;

import java.util.List;

public record GetBoardsResponse(
        List<BoardItemResult> categories
) {
    public static GetBoardsResponse from(GetBoardsResult getBoardsResult) {
        return new GetBoardsResponse(getBoardsResult.boards());
    }
}
