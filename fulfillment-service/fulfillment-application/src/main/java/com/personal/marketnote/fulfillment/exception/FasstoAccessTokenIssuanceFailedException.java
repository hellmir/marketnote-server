package com.personal.marketnote.fulfillment.exception;

public class FasstoAccessTokenIssuanceFailedException extends RuntimeException {
    public FasstoAccessTokenIssuanceFailedException(String eventId, Long targetId) {
        super("Fassto 액세스 토큰 발급 실패. eventId=" + eventId + ", targetId=" + targetId);
    }
}
