package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFasstoDeliveryGoodDetailResult(
        Integer dataCount,
        List<FasstoDeliveryGoodDetailInfoResult> goodDetails
) {
    public static GetFasstoDeliveryGoodDetailResult of(
            Integer dataCount,
            List<FasstoDeliveryGoodDetailInfoResult> goodDetails
    ) {
        return new GetFasstoDeliveryGoodDetailResult(dataCount, goodDetails);
    }
}
