package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AligoBrandTalkSendApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        Info info
) {

    public record Info(
            String type,
            @JsonProperty("mid") String mid,
            @JsonProperty("scnt") int scnt,
            @JsonProperty("fcnt") int fcnt,
            @JsonProperty("unit_cost") int unitCost,
            @JsonProperty("total_cost") int totalCost
    ) {
    }
}
