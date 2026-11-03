package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FaqPostCategory implements PostCategory {
    ORDER_DELIVERY("주문, 결제, 배송 관련 문의") {
        @Override
        public boolean isDefault() {
            return true;
        }
    },
    CANCEL_RETURN("취소, 반품, 환불 관련 문의"),
    BENEFIT_REWARD("앱 내 보상 및 적립 관련 문의"),
    PRODUCT("제품 및 오픈 관련 문의"),
    ACCOUNT("회원정보 및 계정 관련 문의");

    private final String description;

    @Override
    public Boolean isMe(String categoryCode) {
        return FormatValidator.equals(name(), categoryCode);
    }
}
