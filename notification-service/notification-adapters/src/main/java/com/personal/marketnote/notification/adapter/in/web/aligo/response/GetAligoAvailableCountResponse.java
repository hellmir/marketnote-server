package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoAvailableCountResult;

public record GetAligoAvailableCountResponse(
        int smsCnt,
        int lmsCnt,
        int mmsCnt,
        int altCnt
) {

    public static GetAligoAvailableCountResponse from(GetAligoAvailableCountResult result) {
        return new GetAligoAvailableCountResponse(
                result.smsCnt(), result.lmsCnt(), result.mmsCnt(), result.altCnt()
        );
    }
}
