package com.personal.marketnote.community.port.in.result.board;

import com.personal.marketnote.community.domain.post.Board;

import java.util.Arrays;
import java.util.List;

public record GetBoardsResult(
        List<BoardItemResult> boards
) {
    public static GetBoardsResult from(Board[] boards) {
        return new GetBoardsResult(
                Arrays.stream(boards)
                        .map(
                                board -> BoardItemResult.of(
                                        board.name(), board.getDescription()
                                )
                        )
                        .toList()
        );
    }
}
