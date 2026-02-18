package com.personal.marketnote.user.domain.shippingaddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DeliveryRequestType {
    NONE("선택 안 함"),
    LEAVE_AT_SECURITY("부재 시 경비실에 맡겨주세요"),
    CALL_BEFORE_DELIVERY("배송 전에 꼭 연락주세요"),
    LEAVE_AT_DOOR("집 앞에 놔주세요"),
    LEAVE_AT_DELIVERY_BOX("택배함에 놔주세요"),
    CUSTOM("직접입력");

    private final String description;

    public boolean isCustom() {
        return this == CUSTOM;
    }
}
