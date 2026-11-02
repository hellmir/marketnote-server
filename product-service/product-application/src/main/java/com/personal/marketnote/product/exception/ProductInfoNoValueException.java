package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.NoValueException;
import lombok.Getter;

@Getter
public class ProductInfoNoValueException extends NoValueException {
    public ProductInfoNoValueException(String message, String code) {
        super(String.format(message, code));
    }
}
