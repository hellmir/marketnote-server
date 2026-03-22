package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacSignatureMismatchException;
import com.personal.marketnote.common.security.hmac.exception.HmacTimestampExpiredException;

import java.time.Clock;
import java.time.Instant;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.TIMESTAMP_TOLERANCE_SECONDS;

public final class HmacSignatureValidator {

    private HmacSignatureValidator() {
    }

    public static void validate(String secretKey,
                                String timestamp,
                                String nonce,
                                String httpMethod,
                                String requestPath,
                                String signature,
                                Clock clock) {
        validateTimestamp(timestamp, clock);
        validateSignature(secretKey, timestamp, nonce, httpMethod, requestPath, signature);
    }

    static void validateTimestamp(String timestamp, Clock clock) {
        long requestTimeMillis;
        try {
            requestTimeMillis = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new HmacTimestampExpiredException(timestamp);
        }
        long currentTimeMillis = Instant.now(clock).toEpochMilli();
        long differenceSeconds = Math.abs(currentTimeMillis - requestTimeMillis) / 1000;
        if (differenceSeconds > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new HmacTimestampExpiredException(timestamp);
        }
    }

    static void validateSignature(String secretKey,
                                  String timestamp,
                                  String nonce,
                                  String httpMethod,
                                  String requestPath,
                                  String signature) {
        String expected = HmacSignatureGenerator.generate(secretKey, timestamp, nonce, httpMethod, requestPath);
        if (!HmacSignatureGenerator.constantTimeEquals(expected, signature)) {
            throw new HmacSignatureMismatchException();
        }
    }
}
