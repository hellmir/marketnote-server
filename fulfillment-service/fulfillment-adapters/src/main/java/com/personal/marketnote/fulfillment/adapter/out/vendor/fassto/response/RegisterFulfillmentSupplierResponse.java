package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.personal.marketnote.common.utility.FormatValidator;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterFulfillmentSupplierResponse(
        FulfillmentResponseHeader header,
        RegisterFulfillmentSupplierDataResponse data,
        FulfillmentErrorInfo errorInfo
) implements FulfillmentApiResponse {
    public boolean isSuccess() {
        return FormatValidator.hasValue(header) && header.isSuccess() && FormatValidator.hasValue(data) && data.isSuccess();
    }

    @Override
    public String resolveErrorMessage() {
        String message = FulfillmentApiResponse.super.resolveErrorMessage();
        if (FormatValidator.hasValue(message)) {
            return message;
        }

        if (FormatValidator.hasValue(data) && !data.isSuccess()) {
            return FulfillmentApiResponse.normalizeMessage(data.msg());
        }

        return null;
    }
}
