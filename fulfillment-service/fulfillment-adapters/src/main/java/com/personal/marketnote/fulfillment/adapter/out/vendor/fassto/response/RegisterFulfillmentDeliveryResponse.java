package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterFulfillmentDeliveryResponse(
        FulfillmentResponseHeader header,
        @JsonDeserialize(using = RegisterFulfillmentDeliveryDataDeserializer.class)
        List<RegisterFulfillmentDeliveryItemResponse> data,
        FulfillmentErrorInfo errorInfo
) implements FulfillmentApiResponse {
    public boolean isSuccess() {
        if (FormatValidator.hasNoValue(header) || !header.isSuccess()) {
            return false;
        }
        if (FormatValidator.hasNoValue(data)) {
            return true;
        }
        return data.stream().allMatch(RegisterFulfillmentDeliveryItemResponse::isSuccess);
    }

    @Override
    public String resolveErrorMessage() {
        String message = FulfillmentApiResponse.super.resolveErrorMessage();
        if (FormatValidator.hasValue(message)) {
            return message;
        }

        if (FormatValidator.hasValue(data)) {
            for (RegisterFulfillmentDeliveryItemResponse item : data) {
                if (FormatValidator.hasNoValue(item) || item.isSuccess()) {
                    continue;
                }

                String itemMessage = FulfillmentApiResponse.normalizeMessage(item.msg());
                if (FormatValidator.hasValue(itemMessage)) {
                    return itemMessage;
                }
            }
        }

        return null;
    }
}
