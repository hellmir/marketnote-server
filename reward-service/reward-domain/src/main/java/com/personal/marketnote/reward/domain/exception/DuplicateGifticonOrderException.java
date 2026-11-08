package com.personal.marketnote.reward.domain.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateGifticonOrderException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_GIFTICON_ORDER_02::기프티콘 주문이 중복되었습니다.";

    public DuplicateGifticonOrderException() {
        super(MESSAGE);
    }
}
