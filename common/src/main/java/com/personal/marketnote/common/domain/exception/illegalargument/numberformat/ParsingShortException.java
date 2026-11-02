package com.personal.marketnote.common.domain.exception.illegalargument.numberformat;

public class ParsingShortException extends NumberFormatException {
    private static final String PARSING_SHORT_EXCEPTION_MESSAGE
            = "숫자값만 Short 타입으로 변환할 수 있습니다. 현재 변환 대상 값: %s";

    public ParsingShortException(String value) {
        super(String.format(PARSING_SHORT_EXCEPTION_MESSAGE, value));
    }
}
