package com.personal.marketnote.user.domain.shippingaddress.exception;

public class ShippingAddressCompanyNameNoValueException extends IllegalArgumentException {

    public ShippingAddressCompanyNameNoValueException() {
        super("회사 배송지에는 회사명이 필수입니다.");
    }
}
