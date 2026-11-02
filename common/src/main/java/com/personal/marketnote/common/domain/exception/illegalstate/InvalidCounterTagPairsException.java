package com.personal.marketnote.common.domain.exception.illegalstate;

public class InvalidCounterTagPairsException extends IllegalStateException {
    private static final String INVALID_COUNTER_TAG_PAIRS_EXCEPTION_MESSAGE
            = "Counter 태그 키-값 쌍은 짝수 개여야 합니다. 전송된 길이: %d";

    public InvalidCounterTagPairsException(int length) {
        super(String.format(INVALID_COUNTER_TAG_PAIRS_EXCEPTION_MESSAGE, length));
    }
}
