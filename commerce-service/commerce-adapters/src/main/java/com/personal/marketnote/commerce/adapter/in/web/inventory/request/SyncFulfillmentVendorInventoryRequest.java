package com.personal.marketnote.commerce.adapter.in.web.inventory.request;

import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class SyncFulfillmentVendorInventoryRequest {
    @Schema(
            name = "inventories",
            description = "풀필먼트 벤더 재고 목록 (최대 1000건)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "재고 목록은 필수값입니다.")
    @Size(
            max = SyncFulfillmentVendorInventoryCommand.MAX_INVENTORIES_SIZE,
            message = "재고 목록은 최대 1000건까지 허용됩니다."
    )
    @Valid
    private List<SyncFulfillmentVendorInventoryItemRequest> inventories;
}
