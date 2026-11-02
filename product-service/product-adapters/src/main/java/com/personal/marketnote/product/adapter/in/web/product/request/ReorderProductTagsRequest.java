package com.personal.marketnote.product.adapter.in.web.product.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReorderProductTagsRequest(
        @NotNull(message = "태그 순서 목록은 필수값입니다.")
        @Size(min = 1, message = "태그 순서 목록은 최소 1개 이상이어야 합니다.")
        List<@Valid TagOrderItem> tagOrders
) {
    public record TagOrderItem(
            @NotNull(message = "태그 ID는 필수값입니다.")
            Long tagId,

            @NotNull(message = "순서 번호는 필수값입니다.")
            @Min(value = 1, message = "순서 번호는 1 이상이어야 합니다.")
            Long orderNum
    ) {
    }
}
