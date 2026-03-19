package com.personal.marketnote.user.domain.shippingaddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DeliveryRequestType {
    NONE("선택 안 함"),
    LEAVE_AT_DOOR("문 앞에 놓아주세요"),
    RECEIVE_OR_LEAVE_AT_DOOR("직접 받고 부재시 문 앞에 놓아 주세요"),
    LEAVE_AT_SECURITY("경비실에 맡겨주세요"),
    LEAVE_AT_DELIVERY_BOX("택배함에 넣어주세요"),
    CUSTOM("직접 입력");

    private final String description;

    public boolean isCustom() {
        return this == CUSTOM;
    }
}
