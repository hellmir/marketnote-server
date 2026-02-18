package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryIcsCompletionMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFasstoDeliveryIcsResult;

public interface CompleteFasstoDeliveryIcsPort {
    CompleteFasstoDeliveryIcsResult completeDeliveryIcs(FasstoDeliveryIcsCompletionMapper request);
}
