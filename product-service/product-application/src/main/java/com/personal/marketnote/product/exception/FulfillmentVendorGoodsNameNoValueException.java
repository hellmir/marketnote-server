package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.NoValueException;

public class FulfillmentVendorGoodsNameNoValueException extends NoValueException {
    private static final String MESSAGE = "ERR_FULFILLMENT_GOODS_02::풀필먼트 상품명(godNm)은 필수입니다.";

    public FulfillmentVendorGoodsNameNoValueException() {
        super(MESSAGE);
    }
}
