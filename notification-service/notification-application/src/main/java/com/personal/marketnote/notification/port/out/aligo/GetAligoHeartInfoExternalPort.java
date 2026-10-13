package com.personal.marketnote.notification.port.out.aligo;

public interface GetAligoHeartInfoExternalPort {

    AligoHeartInfoResult getHeartInfo();

    record AligoHeartInfoResult(
            int smsCnt,
            int lmsCnt,
            int mmsCnt,
            int altCnt
    ) {
    }
}
