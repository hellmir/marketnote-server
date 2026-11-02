package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.NoValueException;
import lombok.Getter;

@Getter
public class OptionsNoValueException extends NoValueException {
    private static final String OPTIONS_NO_VALUE_EXCEPTION_MESSAGE = "%s:: 각 옵션 카테고리는 최소 1개 이상의 옵션을 포함해야 합니다.";

    public OptionsNoValueException(String code) {
        super(String.format(OPTIONS_NO_VALUE_EXCEPTION_MESSAGE, code));
    }
}
