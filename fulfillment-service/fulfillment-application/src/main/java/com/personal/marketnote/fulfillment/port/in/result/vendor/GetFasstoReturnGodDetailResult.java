package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFasstoReturnGodDetailResult(
        Integer dataCount,
        List<FasstoReturnGodDetailInfoResult> returnGodInfos
) {
    public static GetFasstoReturnGodDetailResult of(
            Integer dataCount,
            List<FasstoReturnGodDetailInfoResult> returnGodInfos
    ) {
        return new GetFasstoReturnGodDetailResult(dataCount, returnGodInfos);
    }
}
