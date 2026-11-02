package com.personal.marketnote.common.domain.delivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PickupRequestType {
    NONE("선택 안 함"),
    CALL_BEFORE_VISIT("방문 전 연락 부탁드립니다"),
    LEAVE_AT_DOOR("문 앞에 놓아주세요"),
    LEAVE_AT_SECURITY("경비실에 맡겨주세요"),
    CUSTOM("직접 입력");

    private final String description;

    public boolean isCustom() {
        return this == CUSTOM;
    }
}
