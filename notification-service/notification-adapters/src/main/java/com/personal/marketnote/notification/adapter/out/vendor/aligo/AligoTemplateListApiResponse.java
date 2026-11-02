package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record AligoTemplateListApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message,
        List<TemplateItem> list
) {

    record TemplateItem(
            String senderkey,
            @JsonProperty("templt_code") String templtCode,
            @JsonProperty("templt_name") String templtName,
            @JsonProperty("templt_content") String templtContent,
            @JsonProperty("template_type") String templateType,
            @JsonProperty("template_emphasize_type") String templateEmphasizeType,
            @JsonProperty("inspStatus") String inspStatus,
            String status,
            String comments
    ) {
    }
}
