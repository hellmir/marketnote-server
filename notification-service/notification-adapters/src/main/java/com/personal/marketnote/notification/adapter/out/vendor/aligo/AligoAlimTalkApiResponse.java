package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

record AligoAlimTalkApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        Info info
) {

    record Info(
            String type,
            @JsonProperty("mid") String mid,
            @JsonProperty("scnt") int scnt,
            @JsonProperty("fcnt") int fcnt,
            @JsonProperty("mdid") String mdid,
            @JsonProperty("smid") String smid
    ) {
    }
}
