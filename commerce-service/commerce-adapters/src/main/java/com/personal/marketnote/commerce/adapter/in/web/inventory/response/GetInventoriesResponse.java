package com.personal.marketnote.commerce.adapter.in.web.inventory.response;

import com.personal.marketnote.commerce.port.in.result.inventory.GetInventoriesResult;
import com.personal.marketnote.commerce.port.in.result.inventory.GetInventoryResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.Set;

@Builder(access = AccessLevel.PRIVATE)
public record GetInventoriesResponse(
        Set<GetInventoryResult> inventories
) {
    public static GetInventoriesResponse from(GetInventoriesResult getInventoriesResult) {
        return new GetInventoriesResponse(getInventoriesResult.inventories());
    }
}

