package com.personal.marketnote.common.domain.exception.illegalargument.numberformat;

public class ParsingFloatException extends NumberFormatException {
    private static final String PARSING_FLOAT_EXCEPTION_MESSAGE
            = "소수값만 float 타입으로 변환할 수 있습니다. 현재 변환 대상 값: %s";

    public ParsingFloatException(String value) {
        super(String.format(PARSING_FLOAT_EXCEPTION_MESSAGE, value));
    }
}
