package com.personal.marketnote.product.adapter.out.response;

import com.personal.marketnote.product.port.out.result.GetInventoryResult;

import java.util.Set;

public record GetInventoriesResponse(
        Set<GetInventoryResult> inventories
) {
}
