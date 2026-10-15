package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkCostTypeException extends IllegalArgumentException {

    public InvalidBrandTalkCostTypeException(String code) {
        super("ERR_BRAND_TALK_COST_TYPE::알 수 없는 비용 유형입니다. code=" + code);
    }
}
