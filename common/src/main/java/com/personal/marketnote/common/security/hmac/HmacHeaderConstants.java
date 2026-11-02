package com.personal.marketnote.common.security.hmac;

public final class HmacHeaderConstants {
    public static final String HEADER_SIGNATURE = "X-HMAC-Signature";
    public static final String HEADER_TIMESTAMP = "X-HMAC-Timestamp";
    public static final String HEADER_NONCE = "X-HMAC-Nonce";
    public static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;
    public static final String SIGNING_INPUT_DELIMITER = ":";
    public static final String REDIS_NONCE_KEY_PREFIX = "hmac:nonce:";

    private HmacHeaderConstants() {
    }
}
