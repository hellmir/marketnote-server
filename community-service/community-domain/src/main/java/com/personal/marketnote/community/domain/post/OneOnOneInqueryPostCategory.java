package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OneOnOneInqueryPostCategory implements PostCategory {
    ORDER_PAYMENT("주문/결제"),
    DELIVERY("배송 관련"),
    CANCEL_REFUND("취소/환불"),
    RETURN_EXCHANGE("반품/교환"),
    POINT("적립금(포인트)"),
    EVENT_COUPON("이벤트/쿠폰");

    private final String description;

    @Override
    public Boolean isMe(String categoryCode) {
        return FormatValidator.equals(name(), categoryCode);
    }
}
