package com.personal.marketnote.community.port.in.result.board;

import com.personal.marketnote.community.domain.post.PostCategory;

import java.util.Arrays;
import java.util.List;

public record GetBoardCategoriesResult(
        List<BoardCategoryItemResult> categories
) {
    public static GetBoardCategoriesResult from(PostCategory[] postCategories) {
        return new GetBoardCategoriesResult(
                Arrays.stream(postCategories)
                        .map(
                                category -> BoardCategoryItemResult.of(
                                        category.getCode(), category.getDescription()
                                )
                        )
                        .toList()
        );
    }
}
