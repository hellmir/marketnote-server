package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

record AligoHeartInfoApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        @JsonProperty("SMS_CNT") int smsCnt,
        @JsonProperty("LMS_CNT") int lmsCnt,
        @JsonProperty("MMS_CNT") int mmsCnt,
        @JsonProperty("ALT_CNT") int altCnt
) {
}
