package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentAccessTokenResult;

public record FulfillmentAuthTokenResponse(
        FulfillmentAccessTokenResult tokenInfo
) {
    public static FulfillmentAuthTokenResponse from(FulfillmentAccessTokenResult tokenInfo) {
        return new FulfillmentAuthTokenResponse(tokenInfo);
    }
}
