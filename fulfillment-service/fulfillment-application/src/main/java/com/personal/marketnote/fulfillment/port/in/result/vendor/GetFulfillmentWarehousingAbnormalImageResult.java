package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record GetFulfillmentWarehousingAbnormalImageResult(
        Integer dataCount,
        Object data
) {
    public static GetFulfillmentWarehousingAbnormalImageResult of(
            Integer dataCount,
            Object data
    ) {
        return new GetFulfillmentWarehousingAbnormalImageResult(dataCount, data);
    }
}
