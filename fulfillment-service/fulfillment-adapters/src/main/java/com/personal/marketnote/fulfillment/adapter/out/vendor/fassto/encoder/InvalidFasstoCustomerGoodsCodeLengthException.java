package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

public class InvalidFasstoCustomerGoodsCodeLengthException extends RuntimeException {
    public InvalidFasstoCustomerGoodsCodeLengthException(int actualLength) {
        super("Fassto cstGodCd length must be at least 22 characters. actualLength=" + actualLength);
    }

    public InvalidFasstoCustomerGoodsCodeLengthException(String reason) {
        super("Fassto cstGodCd length is invalid. reason=" + reason);
    }
}
