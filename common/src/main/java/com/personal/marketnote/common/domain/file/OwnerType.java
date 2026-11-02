package com.personal.marketnote.common.domain.file;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum OwnerType {
    PRODUCT("상품"),
    ORDER("주문"),
    POST("게시글"),
    REVIEW("사용자 후기");

    private final String description;

    public static OwnerType from(String targetValue) throws IllegalArgumentException {
        return Arrays.stream(OwnerType.values())
                .filter(ownerType -> FormatValidator.equals(ownerType.name(), targetValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid owner type: " + targetValue));
    }
}
