package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.NoValueException;

public class FulfillmentVendorGoodsTypeNoValueException extends NoValueException {
    private static final String MESSAGE = "ERR_FULFILLMENT_GOODS_03::풀필먼트 상품 유형(godType)은 필수입니다.";

    public FulfillmentVendorGoodsTypeNoValueException() {
        super(MESSAGE);
    }
}
