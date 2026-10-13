package com.personal.marketnote.notification.port.in.result.aligo;

public record GetAligoAvailableCountResult(
        int smsCnt,
        int lmsCnt,
        int mmsCnt,
        int altCnt
) {
}
