package com.personal.marketnote.community.port.in.result.board;

public record BoardCategoryItemResult(
        String name,
        String description
) {
    public static BoardCategoryItemResult of(String name, String description) {
        return new BoardCategoryItemResult(name, description);
    }
}
