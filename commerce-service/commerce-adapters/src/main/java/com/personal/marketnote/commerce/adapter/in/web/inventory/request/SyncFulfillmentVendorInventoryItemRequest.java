package com.personal.marketnote.commerce.adapter.in.web.inventory.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SyncFulfillmentVendorInventoryItemRequest {
    @Schema(
            name = "productKey",
            description = "상품 키 (UUID)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 키는 필수값입니다.")
    private UUID productKey;

    @Schema(
            name = "stock",
            description = "동기화 대상 재고 수량",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "재고 수량은 필수값입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    @Max(value = Integer.MAX_VALUE, message = "재고 수량은 정수형 최대값을 초과할 수 없습니다.")
    private Integer stock;
}
