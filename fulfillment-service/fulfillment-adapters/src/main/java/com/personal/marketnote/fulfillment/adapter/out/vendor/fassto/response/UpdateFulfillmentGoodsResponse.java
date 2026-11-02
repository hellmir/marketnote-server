package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateFulfillmentGoodsResponse(
        FulfillmentResponseHeader header,
        List<UpdateFulfillmentGoodsItemResponse> data,
        FulfillmentErrorInfo errorInfo
) implements FulfillmentApiResponse {
    public boolean isSuccess() {
        return FormatValidator.hasValue(header) && header.isSuccess() && FormatValidator.hasValue(data)
                && data.stream().allMatch(UpdateFulfillmentGoodsItemResponse::isSuccess);
    }

    @Override
    public String resolveErrorMessage() {
        String message = FulfillmentApiResponse.super.resolveErrorMessage();
        if (FormatValidator.hasValue(message)) {
            return message;
        }

        if (FormatValidator.hasValue(data)) {
            for (UpdateFulfillmentGoodsItemResponse item : data) {
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
