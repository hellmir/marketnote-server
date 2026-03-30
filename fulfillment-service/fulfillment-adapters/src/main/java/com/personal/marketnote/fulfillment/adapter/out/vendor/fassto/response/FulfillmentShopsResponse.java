package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentShopsResponse(
        FulfillmentResponseHeader header,
        List<FulfillmentShopItemResponse> data,
        FulfillmentErrorInfo errorInfo
) implements FulfillmentApiResponse {
    public boolean isSuccess() {
        return FormatValidator.hasValue(header) && header.isSuccess();
    }
}
