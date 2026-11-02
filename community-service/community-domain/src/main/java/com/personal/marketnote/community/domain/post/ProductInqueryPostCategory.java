package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductInqueryPostCategory implements PostCategory {
    PRODUCT_QUESTION("상품 문의"),
    RESTOCK("재입고 문의"),
    SHIPPING("배송 문의");

    private final String description;

    @Override
    public Boolean isMe(String categoryCode) {
        return FormatValidator.equals(name(), categoryCode);
    }
}
