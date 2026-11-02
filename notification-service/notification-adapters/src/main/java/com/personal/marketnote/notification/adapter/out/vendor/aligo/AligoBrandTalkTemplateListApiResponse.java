package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AligoBrandTalkTemplateListApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        PageInfo info,
        List<TemplateItem> list
) {

    public record PageInfo(
            @JsonProperty("current_page") int currentPage,
            @JsonProperty("page_size") int pageSize,
            int total
    ) {
    }

    public record TemplateItem(
            @JsonProperty("tpl_code") String tplCode,
            @JsonProperty("tpl_name") String tplName,
            @JsonProperty("tpl_type") String tplType,
            @JsonProperty("tpl_status") String tplStatus,
            @JsonProperty("send_variable") List<String> sendVariable,
            @JsonProperty("create_at") String createAt
    ) {
    }
}
