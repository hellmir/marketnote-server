package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.NoValueException;

public class FulfillmentVendorGoodsCustomCodeNoValueException extends NoValueException {
    private static final String MESSAGE = "ERR_FULFILLMENT_GOODS_01::풀필먼트 상품 고객사 코드(cstGodCd)는 필수입니다.";

    public FulfillmentVendorGoodsCustomCodeNoValueException() {
        super(MESSAGE);
    }
}
