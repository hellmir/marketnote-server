package com.personal.marketnote.commerce.adapter.in.web.inventory.mapper;

import com.personal.marketnote.commerce.adapter.in.web.inventory.request.SyncFulfillmentVendorInventoryRequest;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryItemCommand;
import com.personal.marketnote.common.adapter.in.request.RegisterInventoryRequest;

import java.util.List;

public class InventoryRequestToCommandMapper {
    public static RegisterInventoryCommand mapToCommand(
            RegisterInventoryRequest request
    ) {
        return RegisterInventoryCommand.of(request.getProductId(), request.getPricePolicyId());
    }

    public static SyncFulfillmentVendorInventoryCommand mapToCommand(
            SyncFulfillmentVendorInventoryRequest request
    ) {
        List<SyncFulfillmentVendorInventoryItemCommand> inventories = request.getInventories().stream()
                .map(item -> SyncFulfillmentVendorInventoryItemCommand.of(item.getProductKey(), item.getStock()))
                .toList();

        return SyncFulfillmentVendorInventoryCommand.of(inventories);
    }
}
