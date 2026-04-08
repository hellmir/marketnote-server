package com.personal.marketnote.reward.domain.exception;

public class GifticonOrderNotFoundException extends RuntimeException {
    private static final String MESSAGE_BY_TR_ID = "기프티콘 주문을 찾을 수 없습니다. trId=%s";
    private static final String MESSAGE_BY_ORDER_ID = "기프티콘 주문을 찾을 수 없습니다. orderId=%d";

    public GifticonOrderNotFoundException(String trId) {
        super(String.format(MESSAGE_BY_TR_ID, trId));
    }

    public GifticonOrderNotFoundException(Long orderId) {
        super(String.format(MESSAGE_BY_ORDER_ID, orderId));
    }
}
