package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FasstoWarehousingInspecDetailListResponse(
        FasstoResponseHeader header,
        List<FasstoWarehousingInspecDetailItemResponse> data,
        FasstoErrorInfo errorInfo
) implements FasstoApiResponse {
    public boolean isSuccess() {
        return header != null && header.isSuccess() && data != null;
    }
}
