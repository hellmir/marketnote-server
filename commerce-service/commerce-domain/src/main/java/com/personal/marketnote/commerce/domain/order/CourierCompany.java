package com.personal.marketnote.commerce.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CourierCompany {
    CJ_LOGISTICS("CJ대한통운"),
    HANJIN("한진택배"),
    LOTTE("롯데택배"),
    LOGEN("로젠택배"),
    POST_OFFICE("우체국택배"),
    ETC("기타");

    private final String description;
}
