package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFasstoDeliveryOutOrdGoodsByOrdNoResult(
        Integer dataCount,
        List<FasstoDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo
) {
    public static GetFasstoDeliveryOutOrdGoodsByOrdNoResult of(
            Integer dataCount,
            List<FasstoDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo
    ) {
        return new GetFasstoDeliveryOutOrdGoodsByOrdNoResult(dataCount, goodsByOrdNo);
    }
}
