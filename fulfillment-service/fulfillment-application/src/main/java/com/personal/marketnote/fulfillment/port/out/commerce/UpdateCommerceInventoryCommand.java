package com.personal.marketnote.fulfillment.port.out.commerce;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;

import java.util.List;

public record UpdateCommerceInventoryCommand(
        List<UpdateCommerceInventoryItemCommand> inventories
) {
    public UpdateCommerceInventoryCommand {
        if (FormatValidator.hasNoValue(inventories)) {
            throw new FasstoQueryParameterNoValueException("Inventory sync items", "commerce inventory sync");
        }
    }

    public static UpdateCommerceInventoryCommand of(
            List<UpdateCommerceInventoryItemCommand> inventories
    ) {
        return new UpdateCommerceInventoryCommand(inventories);
    }
}
