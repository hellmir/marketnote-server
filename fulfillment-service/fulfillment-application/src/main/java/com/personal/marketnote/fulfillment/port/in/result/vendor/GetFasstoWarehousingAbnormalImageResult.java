package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record GetFasstoWarehousingAbnormalImageResult(
        Integer dataCount,
        Object data
) {
    public static GetFasstoWarehousingAbnormalImageResult of(
            Integer dataCount,
            Object data
    ) {
        return new GetFasstoWarehousingAbnormalImageResult(dataCount, data);
    }
}
