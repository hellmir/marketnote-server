package com.personal.marketnote.community.port.in.result.board;

public record BoardCategoryItemResult(
        String name,
        String description,
        boolean isDefault
) {
    public static BoardCategoryItemResult of(String name, String description, boolean isDefault) {
        return new BoardCategoryItemResult(name, description, isDefault);
    }
}
