package com.personal.marketnote.common.security.hmac;

import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.util.UUID;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.*;

public class HmacServiceAuthHeaderBuilder {
    private final String secretKey;
    private final Clock clock;

    public HmacServiceAuthHeaderBuilder(String secretKey, Clock clock) {
        this.secretKey = secretKey;
        this.clock = clock;
    }

    public void applyHeaders(HttpHeaders headers, String httpMethod, String requestPath) {
        String timestamp = String.valueOf(clock.millis());
        String nonce = UUID.randomUUID().toString();
        String signature = HmacSignatureGenerator.generate(secretKey, timestamp, nonce, httpMethod, requestPath);

        headers.set(HEADER_SIGNATURE, signature);
        headers.set(HEADER_TIMESTAMP, timestamp);
        headers.set(HEADER_NONCE, nonce);
    }
}
