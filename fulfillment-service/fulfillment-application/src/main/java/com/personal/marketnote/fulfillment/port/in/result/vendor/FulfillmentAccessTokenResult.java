package com.personal.marketnote.fulfillment.port.in.result.vendor;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;

import java.time.LocalDateTime;

public record FulfillmentAccessTokenResult(
        String accessToken,
        LocalDateTime expiresAt
) {
    public static FulfillmentAccessTokenResult from(FulfillmentAccessToken fulfillmentAccessToken) {
        return new FulfillmentAccessTokenResult(fulfillmentAccessToken.getValue(), fulfillmentAccessToken.getExpiresAt());
    }
}
