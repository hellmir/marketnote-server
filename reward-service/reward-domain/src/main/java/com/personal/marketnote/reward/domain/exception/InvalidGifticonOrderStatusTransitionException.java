package com.personal.marketnote.reward.domain.exception;

import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;

public class InvalidGifticonOrderStatusTransitionException extends IllegalStateException {
    private static final String MESSAGE = "기프티콘 주문 상태 전이가 유효하지 않습니다. 현재 상태=%s";

    public InvalidGifticonOrderStatusTransitionException(GifticonOrderStatus currentStatus) {
        super(String.format(MESSAGE, currentStatus));
    }
}
