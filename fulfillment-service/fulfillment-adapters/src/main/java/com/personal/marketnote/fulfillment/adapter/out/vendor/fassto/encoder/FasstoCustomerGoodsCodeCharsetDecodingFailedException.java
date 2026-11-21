package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

import com.personal.marketnote.common.exception.VendorOperationFailedException;

public class FasstoCustomerGoodsCodeCharsetDecodingFailedException extends VendorOperationFailedException {
    public FasstoCustomerGoodsCodeCharsetDecodingFailedException() {
        super("Fassto cstGodCd charset is invalid. Only alphanumeric (0-9 A-Z a-z) characters are allowed.");
    }
}
