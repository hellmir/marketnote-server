package com.personal.marketnote.community.port.in.command.post;

public record UpdatePostCommand(
        Long id,
        String title,
        String content,
        Boolean isImportant
) {
    public static UpdatePostCommand of(Long id, String title, String content) {
        return new UpdatePostCommand(id, title, content, null);
    }

    public static UpdatePostCommand of(Long id, String title, String content, Boolean isImportant) {
        return new UpdatePostCommand(id, title, content, isImportant);
    }
}
