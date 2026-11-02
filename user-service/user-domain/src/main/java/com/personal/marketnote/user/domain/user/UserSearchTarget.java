package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum UserSearchTarget {
    ID("회원 기본키"),
    NICKNAME("닉네임"),
    EMAIL("이메일 주소"),
    PHONE_NUMBER("전화번호"),
    REFERENCE_CODE("초대 코드");

    private final String description;
    private final String camelCaseValue;

    UserSearchTarget(String description) {
        this.description = description;
        camelCaseValue = FormatConverter.snakeToCamel(this.name());
    }
}
