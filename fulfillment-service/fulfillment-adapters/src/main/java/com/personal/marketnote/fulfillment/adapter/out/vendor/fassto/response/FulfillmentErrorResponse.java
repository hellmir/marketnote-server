package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentErrorResponse(
        FulfillmentResponseHeader header,
        FulfillmentErrorInfo errorInfo
) implements FulfillmentApiResponse {
}
