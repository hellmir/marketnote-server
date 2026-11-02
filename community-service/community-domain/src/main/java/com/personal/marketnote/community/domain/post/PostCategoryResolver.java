package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.exception.BoardOrCategoryNoValueException;

public class PostCategoryResolver {
    public static PostCategory resolve(Board board, String categoryCode) {
        if (FormatValidator.hasNoValue(board) || FormatValidator.hasNoValue(categoryCode)) {
            throw new BoardOrCategoryNoValueException();
        }

        return board.resolveCategory(categoryCode);
    }
}
