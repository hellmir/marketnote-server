package com.personal.marketnote.product.adapter.out.web.fulfillment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GetFasstoGoodsElementsResponse(
        Integer dataCount,
        List<FasstoGoodsElementResponse> elements
) {
    public boolean isSuccess() {
        return FormatValidator.hasValue(elements);
    }
}
