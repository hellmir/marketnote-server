package com.personal.marketnote.fulfillment.exception;

public class FulfillmentAccessTokenIssuanceFailedException extends RuntimeException {
    public FulfillmentAccessTokenIssuanceFailedException(String eventId, Long targetId) {
        super("Fulfillment 액세스 토큰 발급 실패. eventId=" + eventId + ", targetId=" + targetId);
    }
}
