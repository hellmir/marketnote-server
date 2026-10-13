package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record AligoMessageHistoryApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        @JsonProperty("total") int totalCount,
        List<HistoryInfo> list
) {

    record HistoryInfo(
            @JsonProperty("mid") String mid,
            @JsonProperty("type") String type,
            @JsonProperty("sender") String sender,
            @JsonProperty("msg") String content,
            @JsonProperty("scnt") int scnt,
            @JsonProperty("fcnt") int fcnt,
            @JsonProperty("regdate") String regDate
    ) {
    }
}
