package com.personal.marketnote.commerce.port.in.usecase.inventory;

import com.personal.marketnote.commerce.port.in.command.inventory.BackfillInventoryProductKeyCommand;

public interface BackfillInventoryProductKeyUseCase {
    /**
     * 지정된 productId에 해당하는 Inventory 중 productKey가 null인 레코드만 productKey를 채운다.
     * 이미 productKey가 존재하면 멱등하게 skip.
     */
    void backfillProductKey(BackfillInventoryProductKeyCommand command);
}
