package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFasstoWarehousingInspecDetailResult(
        Integer dataCount,
        List<FasstoWarehousingInspecDetailInfoResult> details
) {
    public static GetFasstoWarehousingInspecDetailResult of(
            Integer dataCount,
            List<FasstoWarehousingInspecDetailInfoResult> details
    ) {
        return new GetFasstoWarehousingInspecDetailResult(dataCount, details);
    }
}
