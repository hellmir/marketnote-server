package com.personal.marketnote.fulfillment.port.in.result;

import java.util.List;

public record GetInternalReturnGodDetailResult(
        Integer dataCount,
        List<InternalReturnGodDetailInfoResult> returnGodInfos
) {
    public static GetInternalReturnGodDetailResult of(
            Integer dataCount,
            List<InternalReturnGodDetailInfoResult> returnGodInfos
    ) {
        return new GetInternalReturnGodDetailResult(dataCount, returnGodInfos);
    }
}
