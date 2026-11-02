package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticePostCategory implements PostCategory {
    ANNOUNCEMENT("공지"),
    EVENT("이벤트");

    private final String description;

    @Override
    public Boolean isMe(String categoryCode) {
        return FormatValidator.equals(name(), categoryCode);
    }
}
