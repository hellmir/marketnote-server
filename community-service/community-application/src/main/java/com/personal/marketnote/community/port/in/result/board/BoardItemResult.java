package com.personal.marketnote.community.port.in.result.board;

public record BoardItemResult(
        String name,
        String description
) {
    public static BoardItemResult of(String name, String description) {
        return new BoardItemResult(name, description);
    }
}
