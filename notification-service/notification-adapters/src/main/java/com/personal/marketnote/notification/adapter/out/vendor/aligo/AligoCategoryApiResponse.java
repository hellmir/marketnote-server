package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record AligoCategoryApiResponse(
        int code,
        String message,
        CategoryData data
) {

    record CategoryData(
            @JsonProperty("firstBusinessType") List<CategoryItem> firstBusinessType,
            @JsonProperty("secondBusinessType") List<CategoryItem> secondBusinessType,
            @JsonProperty("thirdBusinessType") List<CategoryItem> thirdBusinessType
    ) {
    }

    record CategoryItem(
            String parentCode,
            String code,
            String name
    ) {
    }
}
