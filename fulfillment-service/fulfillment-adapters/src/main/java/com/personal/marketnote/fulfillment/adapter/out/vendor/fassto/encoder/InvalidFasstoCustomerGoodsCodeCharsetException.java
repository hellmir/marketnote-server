package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

public class InvalidFasstoCustomerGoodsCodeCharsetException extends RuntimeException {
    public InvalidFasstoCustomerGoodsCodeCharsetException() {
        super("Fassto cstGodCd charset is invalid. Only alphanumeric (0-9 A-Z a-z) characters are allowed.");
    }
}
