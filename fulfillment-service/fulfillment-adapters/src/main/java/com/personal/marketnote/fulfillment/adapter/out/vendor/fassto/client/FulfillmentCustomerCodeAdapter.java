package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import lombok.RequiredArgsConstructor;

@VendorAdapter
@RequiredArgsConstructor
public class FulfillmentCustomerCodeAdapter implements GetFulfillmentCustomerCodePort {
    private final FulfillmentAuthProperties fulfillmentAuthProperties;

    @Override
    public String getCustomerCode() {
        return fulfillmentAuthProperties.getCustomerCode();
    }
}
