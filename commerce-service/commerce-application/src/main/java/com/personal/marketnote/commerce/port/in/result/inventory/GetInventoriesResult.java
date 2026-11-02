package com.personal.marketnote.commerce.port.in.result.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.Set;
import java.util.stream.Collectors;

@Builder(access = AccessLevel.PRIVATE)
public record GetInventoriesResult(
        Set<GetInventoryResult> inventories
) {
    public static GetInventoriesResult from(Set<Inventory> inventories) {
        return new GetInventoriesResult(
                inventories.stream()
                        .map(GetInventoryResult::from)
                        .collect(Collectors.toSet())
        );
    }
}
