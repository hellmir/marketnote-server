package com.personal.marketnote.community.domain.post;

public interface PostCategory {
    Boolean isMe(String categoryCode);

    default String getCode() {
        return ((Enum<?>) this).name();
    }

    String getDescription();
}
