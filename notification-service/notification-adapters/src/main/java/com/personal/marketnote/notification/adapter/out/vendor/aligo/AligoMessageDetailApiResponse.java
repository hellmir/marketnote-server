package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record AligoMessageDetailApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        List<DetailInfo> list
) {

    record DetailInfo(
            @JsonProperty("msgid") String msgId,
            @JsonProperty("receiver") String receiver,
            @JsonProperty("rslt") String rslt,
            @JsonProperty("rslt_message") String rsltMessage,
            @JsonProperty("sms_date") String smsDate,
            @JsonProperty("rslt_date") String rsltDate,
            @JsonProperty("report_date") String reportDate
    ) {
    }
}
