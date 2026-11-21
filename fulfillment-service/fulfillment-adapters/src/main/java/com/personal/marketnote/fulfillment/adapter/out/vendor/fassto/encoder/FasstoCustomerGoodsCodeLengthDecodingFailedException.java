package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

import com.personal.marketnote.common.exception.VendorOperationFailedException;

public class FasstoCustomerGoodsCodeLengthDecodingFailedException extends VendorOperationFailedException {
    public FasstoCustomerGoodsCodeLengthDecodingFailedException(int actualLength) {
        super("Fassto cstGodCd length must be at least 22 characters. actualLength=" + actualLength);
    }

    public FasstoCustomerGoodsCodeLengthDecodingFailedException(String reason) {
        super("Fassto cstGodCd length is invalid. reason=" + reason);
    }
}
