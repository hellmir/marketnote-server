package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class CompleteFulfillmentDeliveryIcsRequest {
    @NotEmpty
    private List<String> ordNoList;
}
