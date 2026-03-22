package com.personal.marketnote.fulfillment.port.out.commerce;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;

public record UpdateCommerceInventoryItemCommand(
        Long productId,
        Integer stock
) {
    public UpdateCommerceInventoryItemCommand {
        if (FormatValidator.hasNoValue(productId)) {
            throw new FasstoQueryParameterNoValueException("Product id", "commerce inventory sync");
        }
        if (FormatValidator.hasNoValue(stock)) {
            throw new FasstoQueryParameterNoValueException("Stock", "commerce inventory sync");
        }
        if (stock < 0) {
            throw new FasstoQueryParameterNoValueException("Stock (non-negative)", "commerce inventory sync");
        }
    }

    public static UpdateCommerceInventoryItemCommand of(
            Long productId,
            Integer stock
    ) {
        return new UpdateCommerceInventoryItemCommand(productId, stock);
    }
}
