package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class UserPointNotFoundException extends DomainNotFoundException {
    private static final String USER_POINT_NOT_FOUND_BY_ID_MESSAGE = "회원 포인트 정보를 찾을 수 없습니다. 전송된 회원 ID: %d";
    private static final String USER_POINT_NOT_FOUND_BY_KEY_MESSAGE = "회원 포인트 정보를 찾을 수 없습니다. 전송된 회원 키: %s";

    public UserPointNotFoundException(Long userId) {
        super(String.format(USER_POINT_NOT_FOUND_BY_ID_MESSAGE, userId));
    }

    public UserPointNotFoundException(String userKey) {
        super(String.format(USER_POINT_NOT_FOUND_BY_KEY_MESSAGE, userKey));
    }
}
