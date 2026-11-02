package com.personal.marketnote.commerce.port.in.usecase.inventory;

import com.personal.marketnote.commerce.port.in.command.inventory.ReserveInventoryCommand;

public interface ReserveInventoryUseCase {
    void reserveInventory(ReserveInventoryCommand command);
}
