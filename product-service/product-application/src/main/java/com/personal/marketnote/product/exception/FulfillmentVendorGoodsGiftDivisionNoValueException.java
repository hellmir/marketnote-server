package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.NoValueException;

public class FulfillmentVendorGoodsGiftDivisionNoValueException extends NoValueException {
    private static final String MESSAGE = "ERR_FULFILLMENT_GOODS_04::풀필먼트 상품 선물 구분(giftDiv)은 필수입니다.";

    public FulfillmentVendorGoodsGiftDivisionNoValueException() {
        super(MESSAGE);
    }
}
